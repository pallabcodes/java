package com.netflix.microservices.fraud;

import com.netflix.microservices.fraud.config.FraudServiceConfig;
import com.netflix.microservices.fraud.config.SecurityConfig;
import com.netflix.microservices.fraud.config.WebConfig;
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
 * Netflix Production-Grade Fraud Service Application
 * 
 * This is the main application class for the Netflix Fraud Detection microservice.
 * It demonstrates Netflix production standards for fraud detection including:
 * 1. Real-time fraud detection algorithms
 * 2. Machine learning model integration
 * 3. Risk scoring and assessment
 * 4. Fraud pattern recognition
 * 5. Anomaly detection
 * 6. Performance optimization for high-throughput
 * 7. Caching for fast fraud checks
 * 8. Monitoring and alerting for fraud patterns
 * 
 * For C/C++ engineers:
 * - This is like the main() function in C++
 * - @SpringBootApplication is like marking the entry point
 * - @EnableDiscoveryClient is like service registration in microservices
 * - @EnableFeignClients is like HTTP client configuration
 * - @EnableCaching is like enabling cache mechanisms for performance
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
    FraudServiceConfig.class,
    SecurityConfig.class,
    WebConfig.class
})
public class FraudServiceApplication {

    /**
     * Main method - Application entry point
     * 
     * This method starts the Spring Boot application and demonstrates
     * proper error handling and logging for fraud detection microservices.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        try {
            SpringApplication.run(FraudServiceApplication.class, args);
        } catch (Exception e) {
            System.err.println("Failed to start Fraud Service: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
