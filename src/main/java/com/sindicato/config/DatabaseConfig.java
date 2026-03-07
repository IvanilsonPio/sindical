package com.sindicato.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.sindicato.repository")
@EnableJpaAuditing
@EnableTransactionManagement
@EnableConfigurationProperties({
    ApplicationConfig.JwtProperties.class,
    ApplicationConfig.FileProperties.class
})
public class DatabaseConfig {
    // Database configuration will be handled by Spring Boot auto-configuration
    // This class serves as a central place for JPA and transaction configuration
}