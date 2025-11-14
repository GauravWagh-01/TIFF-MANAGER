package com.example.tiff_manager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.example.tiff_manager.repository")
@EnableTransactionManagement
public class AppConfig {
    // Additional configuration beans can be added here if needed
}