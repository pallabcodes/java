package com.netflix.springframework.demo;

import com.netflix.springframework.demo.service.UserService;
import com.netflix.springframework.demo.lifecycle.LifecycleDemoBean;
import com.netflix.springframework.demo.annotations.ComponentExample;
import com.netflix.springframework.demo.annotations.ServiceExample;
import com.netflix.springframework.demo.annotations.RepositoryExample;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netflix Spring Framework Demo Application
 * 
 * This class demonstrates the key differences between Spring Core, Spring Framework, and Spring Boot
 * for engineers transitioning from C/C++ to Java Spring ecosystem.
 * 
 * KEY CONCEPTS DEMONSTRATED:
 * 1. Spring Core vs Spring Framework vs Spring Boot
 * 2. Bean Annotations and Lifecycle
 * 3. Inversion of Control (IoC) and Dependency Injection (DI)
 * 4. Application Context and Bean Management
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.netflix.springframework.demo")
public class SpringFrameworkDemoApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(SpringFrameworkDemoApplication.class);
    private static final String APPLICATION_NAME = "Netflix Spring Framework Demo";
    private static final String VERSION = "1.0.0";

    public static void main(String[] args) {
        logger.info("=== {} ===", APPLICATION_NAME);
        logger.info("Version: {}", VERSION);
        logger.info("Demonstrating Spring concepts for C/C++ engineers");
        logger.info("Author: Netflix SDE-2 Team");
        logger.info("Target Audience: Engineers transitioning from C/C++ to Java Spring ecosystem");
        
        try {
            // SPRING BOOT APPROACH (Recommended for Production)
            // Spring Boot automatically configures the application context, 
            // sets up embedded server, and handles many configurations
            logger.info("1. SPRING BOOT APPROACH:");
            logger.info("   - Auto-configuration enabled");
            logger.info("   - Embedded Tomcat server");
            logger.info("   - Production-ready with minimal configuration");
            logger.info("   - REST API endpoints available");
            logger.info("   - Web server running on port 8080");
            
            ConfigurableApplicationContext context = SpringApplication.run(SpringFrameworkDemoApplication.class, args);
            
            // Demonstrate bean lifecycle and dependency injection
            demonstrateSpringConcepts(context);
            
            // Demonstrate web server functionality
            demonstrateWebServer(context);
            
            // Keep the application running for web server demo
            logger.info("=== WEB SERVER RUNNING ===");
            logger.info("Web server is running on http://localhost:8080");
            logger.info("Available endpoints:");
            logger.info("  - GET    /api/v1/users           - Get all users");
            logger.info("  - GET    /api/v1/users/{id}      - Get user by ID");
            logger.info("  - POST   /api/v1/users           - Create new user");
            logger.info("  - PUT    /api/v1/users/{id}      - Update user");
            logger.info("  - DELETE /api/v1/users/{id}      - Delete user");
            logger.info("  - GET    /api/v1/users/search    - Search users");
            logger.info("  - GET    /api/v1/annotations/*   - Spring Boot annotations demo");
            logger.info("  - GET    /actuator/health        - Health check");
            logger.info("Press Ctrl+C to stop the server");
            
            // Graceful shutdown on JVM exit
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down Spring Boot application...");
                try {
                    context.close();
                    logger.info("Application shutdown completed successfully");
                } catch (Exception e) {
                    logger.error("Error during application shutdown", e);
                }
            }));
            
        } catch (Exception e) {
            logger.error("Failed to start Spring Boot application", e);
            System.exit(1);
        }
    }
    
    /**
     * Demonstrates core Spring concepts through practical examples
     * 
     * @param context The Spring Application Context containing all managed beans
     */
    private static void demonstrateSpringConcepts(ConfigurableApplicationContext context) {
        logger.info("=== SPRING CONCEPTS DEMONSTRATION ===");
        
        try {
            // 1. Application Context and Bean Listing
            demonstrateApplicationContext(context);
            
            // 2. Dependency Injection Examples
            demonstrateDependencyInjection(context);
            
            // 3. Bean Lifecycle Demonstration
            demonstrateBeanLifecycle(context);
            
            // 4. Different Bean Annotation Types
            demonstrateBeanAnnotations(context);
            
            logger.info("Spring concepts demonstration completed successfully");
        } catch (Exception e) {
            logger.error("Error during Spring concepts demonstration", e);
            throw new RuntimeException("Failed to demonstrate Spring concepts", e);
        }
    }
    
    /**
     * Demonstrates Application Context and how to list all beans
     * Similar to how you might inspect loaded modules in C/C++
     */
    private static void demonstrateApplicationContext(ConfigurableApplicationContext context) {
        logger.info("2. APPLICATION CONTEXT DEMONSTRATION:");
        logger.info("   - Application Context is like a container for all your objects");
        logger.info("   - Similar to a module registry in C/C++ but with automatic management");
        
        try {
            String[] beanNames = context.getBeanDefinitionNames();
            logger.info("   - Total beans managed by Spring: {}", beanNames.length);
            logger.info("   - Sample beans:");
            
            int maxBeansToShow = Math.min(5, beanNames.length);
            for (int i = 0; i < maxBeansToShow; i++) {
                logger.info("     * {}", beanNames[i]);
            }
            
            if (beanNames.length > 5) {
                logger.info("     * ... and {} more beans", beanNames.length - 5);
            }
        } catch (Exception e) {
            logger.error("Error demonstrating application context", e);
            throw new RuntimeException("Failed to demonstrate application context", e);
        }
    }
    
    /**
     * Demonstrates Dependency Injection - the core of Spring Framework
     * This is similar to constructor injection or setter injection patterns in C++
     */
    private static void demonstrateDependencyInjection(ConfigurableApplicationContext context) {
        logger.info("3. DEPENDENCY INJECTION DEMONSTRATION:");
        logger.info("   - Spring automatically creates and injects dependencies");
        logger.info("   - Similar to dependency injection in C++ but automatic");
        
        try {
            // Get a service that has dependencies injected
            UserService userService = context.getBean(UserService.class);
            if (userService == null) {
                throw new IllegalStateException("UserService bean not found in application context");
            }
            
            logger.info("   - UserService bean retrieved successfully: {}", userService.getClass().getSimpleName());
            userService.performOperation();
            logger.info("   - Dependency injection demonstration completed successfully");
        } catch (Exception e) {
            logger.error("Error demonstrating dependency injection", e);
            throw new RuntimeException("Failed to demonstrate dependency injection", e);
        }
    }
    
    /**
     * Demonstrates Bean Lifecycle hooks
     * Similar to constructor/destructor patterns in C++
     */
    private static void demonstrateBeanLifecycle(ConfigurableApplicationContext context) {
        logger.info("4. BEAN LIFECYCLE DEMONSTRATION:");
        logger.info("   - Beans have lifecycle hooks similar to constructors/destructors in C++");
        logger.info("   - Spring manages the entire lifecycle automatically");
        
        try {
            LifecycleDemoBean lifecycleBean = context.getBean(LifecycleDemoBean.class);
            if (lifecycleBean == null) {
                throw new IllegalStateException("LifecycleDemoBean not found in application context");
            }
            
            logger.info("   - Bean created and initialized: {}", lifecycleBean.getName());
            logger.info("   - Bean initialization status: {}", lifecycleBean.isInitialized());
        } catch (Exception e) {
            logger.error("Error demonstrating bean lifecycle", e);
            throw new RuntimeException("Failed to demonstrate bean lifecycle", e);
        }
    }
    
    /**
     * Demonstrates different types of Bean Annotations
     * These are like different ways to register objects in C++ containers
     */
    private static void demonstrateBeanAnnotations(ConfigurableApplicationContext context) {
        logger.info("5. BEAN ANNOTATIONS DEMONSTRATION:");
        logger.info("   - Different annotations for different types of components");
        logger.info("   - Similar to different registration mechanisms in C++");
        
        try {
            // @Component example
            ComponentExample component = context.getBean(ComponentExample.class);
            if (component == null) {
                throw new IllegalStateException("ComponentExample bean not found");
            }
            logger.info("   - @Component bean: {}", component.getType());
            
            // @Service example
            ServiceExample service = context.getBean(ServiceExample.class);
            if (service == null) {
                throw new IllegalStateException("ServiceExample bean not found");
            }
            logger.info("   - @Service bean: {}", service.getType());
            
            // @Repository example
            RepositoryExample repository = context.getBean(RepositoryExample.class);
            if (repository == null) {
                throw new IllegalStateException("RepositoryExample bean not found");
            }
            logger.info("   - @Repository bean: {}", repository.getType());
            
            logger.info("   - Bean annotations demonstration completed successfully");
        } catch (Exception e) {
            logger.error("Error demonstrating bean annotations", e);
            throw new RuntimeException("Failed to demonstrate bean annotations", e);
        }
    }
    
    /**
     * Demonstrates web server functionality
     * 
     * @param context The Spring Application Context
     */
    private static void demonstrateWebServer(ConfigurableApplicationContext context) {
        logger.info("6. WEB SERVER DEMONSTRATION:");
        logger.info("   - Embedded Tomcat server started");
        logger.info("   - REST API endpoints configured");
        logger.info("   - JSON serialization/deserialization enabled");
        logger.info("   - CORS configuration applied");
        logger.info("   - Health checks available");
        
        try {
            // Get server port
            String serverPort = context.getEnvironment().getProperty("server.port", "8080");
            logger.info("   - Server running on port: {}", serverPort);
            
            // Demonstrate web server features
            logger.info("   - Web server features:");
            logger.info("     * Auto-configuration: ENABLED");
            logger.info("     * Embedded server: TOMCAT");
            logger.info("     * JSON handling: JACKSON");
            logger.info("     * Error handling: CUSTOM");
            logger.info("     * CORS support: ENABLED");
            logger.info("     * Health monitoring: ACTUATOR");
            
            logger.info("   - Web server demonstration completed successfully");
        } catch (Exception e) {
            logger.error("Error demonstrating web server functionality", e);
            throw new RuntimeException("Failed to demonstrate web server functionality", e);
        }
    }
}
