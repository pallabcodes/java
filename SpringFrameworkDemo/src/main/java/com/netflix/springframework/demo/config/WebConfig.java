package com.netflix.springframework.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * WebConfig - Spring Boot Web Configuration
 * 
 * This class demonstrates:
 * 1. Spring Boot web configuration
 * 2. CORS (Cross-Origin Resource Sharing) configuration
 * 3. WebMvcConfigurer interface
 * 4. Embedded web server configuration
 * 
 * For C/C++ engineers:
 * - This is like web server configuration in C++
 * - CORS is like allowing cross-origin requests
 * - Similar to web server configuration files
 * - WebMvcConfigurer is like a configuration interface
 * 
 * @author Netflix SDE-2 Team
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * Configure CORS (Cross-Origin Resource Sharing)
     * 
     * This allows frontend applications to make requests to the API
     * from different origins (domains, ports, protocols).
     * 
     * For C/C++ engineers:
     * - CORS is like allowing cross-origin requests in web servers
     * - Similar to CORS configuration in web frameworks
     * - Allows frontend and backend to be on different servers
     * 
     * @param registry CORS registry for configuration
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        System.out.println("Configuring CORS mappings for web server");
        
        registry.addMapping("/api/**")
                .allowedOrigins("*") // Allow all origins (in production, specify exact origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600); // Cache preflight response for 1 hour
        
        System.out.println("CORS configured for /api/** endpoints");
    }
}
