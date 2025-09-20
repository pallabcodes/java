package com.netflix.springframework.demo;

import com.netflix.springframework.demo.service.UserService;
import com.netflix.springframework.demo.repository.UserRepository;
import com.netflix.springframework.demo.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import static org.junit.jupiter.api.Assertions.*;

/**
 * SpringFrameworkDemoTest - Demonstrates Spring Testing
 * 
 * This test class demonstrates how to test Spring applications and components.
 * It shows both Spring Boot testing and Spring Core testing approaches.
 * 
 * For C/C++ engineers:
 * - Spring testing is similar to unit testing in C++ but with automatic dependency injection
 * - @SpringBootTest is like running the entire application in test mode
 * - @ContextConfiguration is like setting up a test environment
 * - Mocking is similar to mocking in C++ but automatic
 * 
 * @author Netflix SDE-2 Team
 */
@SpringBootTest
class SpringFrameworkDemoTest {

    @Autowired
    private UserService userService;

    @Test
    void contextLoads() {
        // This test verifies that the Spring context loads successfully
        // Similar to verifying that all modules are loaded in C++
        assertNotNull(userService, "UserService should be injected");
    }

    @Test
    void testUserServiceOperation() {
        // Test the UserService business logic
        // Similar to testing business logic in C++
        assertDoesNotThrow(() -> {
            userService.performOperation();
        }, "UserService operation should not throw exception");
    }

    @Test
    void testUserServiceDependencyInjection() {
        // Test that dependencies are properly injected
        // Similar to verifying that dependencies are set in C++
        assertNotNull(userService.getUserRepository(), "UserRepository should be injected");
    }

    /**
     * Test Spring Core configuration without Spring Boot
     * 
     * This demonstrates how to test Spring Core applications
     * Similar to testing individual modules in C++
     */
    @Test
    void testSpringCoreConfiguration() {
        // Create Spring Core application context
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfiguration.class);
        
        try {
            // Test bean creation
            UserService testUserService = context.getBean(UserService.class);
            assertNotNull(testUserService, "UserService should be created");
            
            // Test dependency injection
            assertNotNull(testUserService.getUserRepository(), "UserRepository should be injected");
            
            // Test business logic
            assertDoesNotThrow(() -> {
                testUserService.performOperation();
            }, "UserService operation should work");
            
        } finally {
            // Clean up context
            context.close();
        }
    }

    /**
     * Test bean lifecycle
     * 
     * This demonstrates how to test bean lifecycle hooks
     * Similar to testing constructor/destructor in C++
     */
    @Test
    void testBeanLifecycle() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfiguration.class);
        
        try {
            // Test bean creation and lifecycle
            LifecycleTestBean lifecycleBean = context.getBean(LifecycleTestBean.class);
            assertNotNull(lifecycleBean, "LifecycleTestBean should be created");
            assertTrue(lifecycleBean.isInitialized(), "Bean should be initialized");
            
        } finally {
            // Clean up context (triggers destroy methods)
            context.close();
        }
    }

    /**
     * Test configuration class for testing
     * 
     * This is similar to a test configuration in C++
     */
    @Configuration
    static class TestConfiguration {
        
        @Bean
        public UserRepository userRepository() {
            return new UserRepository();
        }
        
        @Bean
        public UserService userService(UserRepository userRepository) {
            return new UserService(userRepository);
        }
        
        @Bean
        public LifecycleTestBean lifecycleTestBean() {
            return new LifecycleTestBean();
        }
    }

    /**
     * Test bean for lifecycle testing
     * 
     * This demonstrates bean lifecycle in a test environment
     */
    static class LifecycleTestBean {
        private boolean initialized = false;
        
        public void init() {
            this.initialized = true;
        }
        
        public void destroy() {
            this.initialized = false;
        }
        
        public boolean isInitialized() {
            return initialized;
        }
    }
}
