package com.healthcare.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("production")
public class DataSourceConfig {

    /**
     * Render의 DATABASE_URL 환경 변수를 처리합니다.
     * DATABASE_URL 형식: postgresql://user:password@host:port/database
     * Spring Boot는 jdbc:postgresql:// 형식을 기대하므로 변환이 필요합니다.
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // DATABASE_URL 환경 변수 확인
        String databaseUrl = System.getenv("DATABASE_URL");
        
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            try {
                // postgresql:// 형식을 올바르게 파싱
                // 형식: postgresql://user:password@host:port/database 또는 postgresql://user:password@host/database
                String urlWithoutScheme = databaseUrl;
                if (urlWithoutScheme.startsWith("postgresql://")) {
                    urlWithoutScheme = urlWithoutScheme.substring("postgresql://".length());
                } else if (urlWithoutScheme.startsWith("jdbc:postgresql://")) {
                    urlWithoutScheme = urlWithoutScheme.substring("jdbc:postgresql://".length());
                }
                
                // @ 기호로 user:password와 host:port/database 분리
                int atIndex = urlWithoutScheme.indexOf('@');
                if (atIndex == -1) {
                    throw new IllegalArgumentException("Invalid DATABASE_URL format: missing @");
                }
                
                String userPassword = urlWithoutScheme.substring(0, atIndex);
                String hostDatabase = urlWithoutScheme.substring(atIndex + 1);
                
                // user:password 분리
                String[] userPass = userPassword.split(":", 2);
                String username = userPass[0];
                String password = userPass.length > 1 ? userPass[1] : "";
                
                // URL 디코딩 (필요한 경우)
                if (!password.isEmpty()) {
                    try {
                        password = java.net.URLDecoder.decode(password, java.nio.charset.StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        // 디코딩 실패 시 원본 사용
                    }
                }
                
                // host:port/database 분리
                int slashIndex = hostDatabase.indexOf('/');
                if (slashIndex == -1) {
                    throw new IllegalArgumentException("Invalid DATABASE_URL format: missing database name");
                }
                
                String hostPort = hostDatabase.substring(0, slashIndex);
                String database = hostDatabase.substring(slashIndex + 1);
                
                // host:port 분리
                String host;
                int port = 5432; // PostgreSQL 기본 포트
                int colonIndex = hostPort.indexOf(':');
                if (colonIndex != -1) {
                    host = hostPort.substring(0, colonIndex);
                    try {
                        port = Integer.parseInt(hostPort.substring(colonIndex + 1));
                    } catch (NumberFormatException e) {
                        // 포트 파싱 실패 시 기본값 사용
                        System.err.println("Warning: Invalid port in DATABASE_URL, using default 5432");
                    }
                } else {
                    host = hostPort;
                }
                
                // 최종 JDBC URL 구성
                String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
                
                config.setJdbcUrl(jdbcUrl);
                config.setUsername(username);
                if (!password.isEmpty()) {
                    config.setPassword(password);
                }
                config.setDriverClassName("org.postgresql.Driver");
                
            } catch (Exception e) {
                // 파싱 실패 시 에러 출력
                System.err.println("Failed to parse DATABASE_URL: " + databaseUrl);
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to configure DataSource from DATABASE_URL: " + e.getMessage(), e);
            }
        } else {
            // DATABASE_URL이 없으면 환경 변수에서 직접 가져오기
            String url = System.getenv("SPRING_DATASOURCE_URL");
            if (url != null && !url.isEmpty()) {
                config.setJdbcUrl(url);
            } else {
                throw new RuntimeException("DATABASE_URL or SPRING_DATASOURCE_URL environment variable is required");
            }
            
            String username = System.getenv("DATABASE_USERNAME");
            if (username == null || username.isEmpty()) {
                username = System.getenv("SPRING_DATASOURCE_USERNAME");
            }
            if (username != null && !username.isEmpty()) {
                config.setUsername(username);
            }
            
            String password = System.getenv("DATABASE_PASSWORD");
            if (password == null || password.isEmpty()) {
                password = System.getenv("SPRING_DATASOURCE_PASSWORD");
            }
            if (password != null && !password.isEmpty()) {
                config.setPassword(password);
            }
            
            config.setDriverClassName("org.postgresql.Driver");
        }
        
        // HikariCP 설정
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        return new HikariDataSource(config);
    }
}

