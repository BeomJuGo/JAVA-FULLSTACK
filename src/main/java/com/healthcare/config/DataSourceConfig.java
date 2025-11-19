package com.healthcare.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

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
                // postgresql:// 형식을 jdbc:postgresql:// 형식으로 변환
                String jdbcUrl;
                if (databaseUrl.startsWith("postgresql://")) {
                    jdbcUrl = "jdbc:" + databaseUrl;
                } else if (databaseUrl.startsWith("jdbc:postgresql://")) {
                    jdbcUrl = databaseUrl;
                } else {
                    jdbcUrl = "jdbc:postgresql://" + databaseUrl;
                }
                
                // URI 파싱하여 username, password 추출
                URI uri = new URI(jdbcUrl.replace("jdbc:", ""));
                String userInfo = uri.getUserInfo();
                
                if (userInfo != null) {
                    String[] userPass = userInfo.split(":");
                    if (userPass.length >= 1) {
                        config.setUsername(userPass[0]);
                    }
                    if (userPass.length >= 2) {
                        // URL 인코딩된 비밀번호 처리
                        String password = userPass[1];
                        // URL 디코딩이 필요한 경우
                        password = java.net.URLDecoder.decode(password, java.nio.charset.StandardCharsets.UTF_8);
                        config.setPassword(password);
                    }
                }
                
                config.setJdbcUrl(jdbcUrl);
                config.setDriverClassName("org.postgresql.Driver");
                
            } catch (Exception e) {
                // 파싱 실패 시 기본값 사용
                System.err.println("Failed to parse DATABASE_URL: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to configure DataSource from DATABASE_URL", e);
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

