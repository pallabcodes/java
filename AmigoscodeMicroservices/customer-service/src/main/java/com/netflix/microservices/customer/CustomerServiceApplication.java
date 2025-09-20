package com.netflix.microservices.customer;

import com.netflix.microservices.customer.config.CustomerServiceConfig;
import com.netflix.microservices.customer.config.SecurityConfig;
import com.netflix.microservices.customer.config.WebConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Netflix Production-Grade Customer Service Application
 * 
 * This is the main application class for the Netflix Customer microservice.
 * It demonstrates Netflix production standards for microservices including:
 * 1. Service discovery and registration
 * 2. Circuit breaker patterns
 * 3. Distributed tracing
 * 4. Security and authentication
 * 5. Caching and performance optimization
 * 6. Monitoring and observability
 * 7. Configuration management
 * 8. Error handling and resilience
 * 
 * For C/C++ engineers:
 * - This is like the main() function in C++
 * - @SpringBootApplication is like marking the entry point
 * - @EnableDiscoveryClient is like service registration in microservices
 * - @EnableFeignClients is like HTTP client configuration
 * - @EnableCaching is like enabling cache mechanisms
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties
@Import({
    CustomerServiceConfig.class,
    SecurityConfig.class,
    WebConfig.class
})
public class CustomerServiceApplication {

    /**
     * Main method - Application entry point
     * 
     * This method starts the Spring Boot application and demonstrates
     * proper error handling and logging for microservices.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        try {
            SpringApplication.run(CustomerServiceApplication.class, args);
        } catch (Exception e) {
            System.err.println("Failed to start Customer Service: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
