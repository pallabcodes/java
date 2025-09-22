package com.netflix.productivity;

import com.netflix.productivity.config.MultiTenancyConfig;
import com.netflix.productivity.config.SecurityConfig;
import com.netflix.productivity.config.WebConfig;
import com.netflix.productivity.config.CacheConfig;
import com.netflix.productivity.config.AsyncConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Netflix Production-Grade Productivity Platform Application
 * 
 * This is the main application class for the Netflix Productivity Platform (JIRA-like).
 * It demonstrates Netflix production standards for productivity applications including:
 * 1. Multi-tenant architecture with data isolation
 * 2. Issue tracking and project management
 * 3. Workflow automation and customization
 * 4. Real-time collaboration and notifications
 * 5. Advanced search and filtering capabilities
 * 6. Performance optimization for high-throughput
 * 7. Caching for fast data access
 * 8. Monitoring and alerting for productivity metrics
 * 
 * For C/C++ engineers:
 * - This is like the main() function in C++
 * - @SpringBootApplication is like marking the entry point
 * - @EnableCaching is like enabling cache mechanisms for performance
 * - Multi-tenancy is like having multiple isolated workspaces
 * - Productivity platform is like a project management system
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties
@Import({
    MultiTenancyConfig.class,
    SecurityConfig.class,
    WebConfig.class,
    CacheConfig.class,
    AsyncConfig.class
})
public class ProductivityPlatformApplication {

    /**
     * Main method - Application entry point
     * 
     * This method starts the Spring Boot application and demonstrates
     * proper error handling and logging for productivity platform applications.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        try {
            SpringApplication.run(ProductivityPlatformApplication.class, args);
        } catch (Exception e) {
            System.err.println("Failed to start Productivity Platform: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
