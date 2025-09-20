package com.netflix.springframework.demo.config;

import com.netflix.springframework.demo.service.UserService;
import com.netflix.springframework.demo.repository.UserRepository;
import com.netflix.springframework.demo.model.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * SpringCoreConfiguration - Demonstrates Spring Core Concepts
 * 
 * This class demonstrates the core Spring Framework concepts without Spring Boot.
 * It shows how to configure beans manually using Java configuration.
 * 
 * For C/C++ engineers:
 * - This is like a factory class that creates and configures objects
 * - Similar to dependency injection containers in C++
 * - @Configuration tells Spring this class contains bean definitions
 * - @Bean methods are like factory methods that create objects
 * 
 * SPRING CORE CONCEPTS DEMONSTRATED:
 * 1. @Configuration - Marks this class as a configuration class
 * 2. @Bean - Defines beans (objects) that Spring will manage
 * 3. Dependency Injection - How beans depend on other beans
 * 4. Bean Scopes - Singleton vs Prototype scope
 * 5. Bean Lifecycle - How Spring manages object lifecycle
 * 
 * @author Netflix SDE-2 Team
 */
@Configuration
public class SpringCoreConfiguration {
    
    /**
     * Bean definition for UserRepository
     * 
     * This method creates a UserRepository bean that Spring will manage.
     * Similar to a factory method in C++ that creates objects.
     * 
     * In C++, this would be like:
     * class SpringCoreConfiguration {
     * public:
     *     UserRepository* createUserRepository() {
     *         return new UserRepository();
     *     }
     * };
     * 
     * @return UserRepository instance
     */
    @Bean
    public UserRepository userRepository() {
        System.out.println("Creating UserRepository bean - Spring Core Configuration");
        return new UserRepository();
    }
    
    /**
     * Bean definition for UserService with dependency injection
     * 
     * This method creates a UserService bean and injects the UserRepository dependency.
     * Spring automatically calls userRepository() method to get the dependency.
     * 
     * In C++, this would be like:
     * class SpringCoreConfiguration {
     * public:
     *     UserService* createUserService() {
     *         UserRepository* repo = createUserRepository();
     *         return new UserService(repo);
     *     }
     * };
     * 
     * @param userRepository The injected UserRepository dependency
     * @return UserService instance with injected dependency
     */
    @Bean
    public UserService userService(UserRepository userRepository) {
        System.out.println("Creating UserService bean with dependency injection - Spring Core Configuration");
        System.out.println("UserRepository dependency injected: " + (userRepository != null ? "SUCCESS" : "FAILED"));
        return new UserService(userRepository);
    }
    
    /**
     * Bean definition for User with prototype scope
     * 
     * This demonstrates different bean scopes in Spring.
     * By default, beans are singleton (one instance per container).
     * Prototype scope creates a new instance each time it's requested.
     * 
     * In C++, this would be like:
     * class SpringCoreConfiguration {
     * public:
     *     User* createUser() {
     *         return new User(); // New instance each time
     *     }
     * };
     * 
     * @return User instance with prototype scope
     */
    @Bean
    @Scope("prototype")
    public User user() {
        System.out.println("Creating User bean with prototype scope - Spring Core Configuration");
        return new User(1L, "Spring Core User", "spring.core@netflix.com");
    }
    
    /**
     * Bean definition for User with singleton scope (default)
     * 
     * This demonstrates singleton scope (default behavior).
     * Only one instance of this bean will be created and reused.
     * 
     * In C++, this would be like:
     * class SpringCoreConfiguration {
     * private:
     *     User* singletonUser = nullptr;
     * public:
     *     User* getSingletonUser() {
     *         if (singletonUser == nullptr) {
     *             singletonUser = new User();
     *         }
     *         return singletonUser;
     *     }
     * };
     * 
     * @return User instance with singleton scope
     */
    @Bean
    @Scope("singleton")
    public User singletonUser() {
        System.out.println("Creating User bean with singleton scope - Spring Core Configuration");
        return new User(2L, "Singleton User", "singleton@netflix.com");
    }
    
    /**
     * Bean definition with custom initialization
     * 
     * This demonstrates how to perform custom initialization after bean creation.
     * Similar to constructor logic in C++ but with more control.
     * 
     * @return Custom configured User instance
     */
    @Bean
    public User customUser() {
        System.out.println("Creating custom User bean - Spring Core Configuration");
        
        User user = new User();
        user.setId(3L);
        user.setName("Custom User");
        user.setEmail("custom@netflix.com");
        
        System.out.println("Custom User bean configured: " + user);
        return user;
    }
    
    /**
     * Bean definition with method parameters
     * 
     * This demonstrates how Spring can inject dependencies into bean creation methods.
     * The userRepository parameter is automatically injected by Spring.
     * 
     * @param userRepository The injected UserRepository dependency
     * @return User instance with repository access
     */
    @Bean
    public User userWithRepository(UserRepository userRepository) {
        System.out.println("Creating User bean with repository access - Spring Core Configuration");
        
        User user = new User(4L, "User with Repository", "user.repo@netflix.com");
        
        // Demonstrate using the injected repository
        User foundUser = userRepository.findById(user.getId());
        System.out.println("User found via repository: " + foundUser);
        
        return user;
    }
}
