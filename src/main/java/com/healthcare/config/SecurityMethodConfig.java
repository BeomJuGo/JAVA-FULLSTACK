// src/main/java/com/healthcare/config/SecurityMethodConfig.java
package com.healthcare.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity // @PreAuthorize 활성화
public class SecurityMethodConfig { }
