package com.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring Boot Application for Netflix-style Backend Service
 * 
 * This application demonstrates enterprise-grade architecture patterns including:
 * - Microservices architecture
 * - RESTful API design
 * - Health monitoring and metrics
 * - Enterprise-grade security
 * 
 * @author Backend Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaRepositories
public class BackendServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendServiceApplication.class, args);
    }
}
