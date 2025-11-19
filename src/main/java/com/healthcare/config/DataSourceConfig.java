package com.healthcare.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

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
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        DataSourceProperties properties = new DataSourceProperties();
        
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
                        properties.setUsername(userPass[0]);
                    }
                    if (userPass.length >= 2) {
                        properties.setPassword(userPass[1]);
                    }
                }
                
                properties.setUrl(jdbcUrl);
                properties.setDriverClassName("org.postgresql.Driver");
                
            } catch (Exception e) {
                // 파싱 실패 시 기본값 사용
                System.err.println("Failed to parse DATABASE_URL: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Username과 Password가 설정되지 않았으면 환경 변수에서 가져오기
        if (properties.getUsername() == null || properties.getUsername().isEmpty()) {
            String username = System.getenv("DATABASE_USERNAME");
            if (username != null && !username.isEmpty()) {
                properties.setUsername(username);
            }
        }
        
        if (properties.getPassword() == null) {
            String password = System.getenv("DATABASE_PASSWORD");
            if (password != null && !password.isEmpty()) {
                properties.setPassword(password);
            }
        }
        
        return properties;
    }
}

