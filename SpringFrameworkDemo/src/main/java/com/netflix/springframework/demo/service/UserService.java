package com.netflix.springframework.demo.service;

import com.netflix.springframework.demo.repository.UserRepository;
import com.netflix.springframework.demo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/**
 * UserService - Demonstrates Service Layer Pattern
 * 
 * This class demonstrates:
 * 1. @Service annotation - marks this as a service layer component
 * 2. Dependency Injection via @Autowired
 * 3. Service layer pattern commonly used in enterprise applications
 * 
 * For C/C++ engineers:
 * - This is similar to a service class that depends on a repository
 * - @Autowired is like automatic dependency injection (similar to DI containers in C++)
 * - @Service tells Spring this is a service component (like registering in a service registry)
 * 
 * @author Netflix SDE-2 Team
 */
@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String SERVICE_NAME = "UserService";
    
    private final UserRepository userRepository;
    
    /**
     * Constructor-based dependency injection (Recommended approach)
     * 
     * This is the preferred way in Spring (similar to constructor injection in C++)
     * It makes dependencies explicit and immutable
     * 
     * @param userRepository The user repository dependency (must not be null)
     * @throws IllegalArgumentException if userRepository is null
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository cannot be null");
        logger.info("{} constructor called - Dependency injected: {}", 
                   SERVICE_NAME, userRepository.getClass().getSimpleName());
    }
    
    /**
     * Business logic method that uses the injected dependency
     * 
     * This demonstrates how Spring manages the entire object graph
     * Similar to how you might use injected dependencies in C++
     */
    public void performOperation() {
        logger.info("UserService.performOperation() called");
        
        try {
            // Use the injected repository
            User user = userRepository.findById(1L);
            if (user != null) {
                logger.info("Retrieved user: {}", user.getName());
            } else {
                logger.warn("No user found with ID: 1");
            }
            
            // Perform some business logic
            logger.info("Processing user data...");
            logger.info("User operation completed successfully");
        } catch (Exception e) {
            logger.error("Error in performOperation", e);
            throw new RuntimeException("Failed to perform user operation", e);
        }
    }
    
    /**
     * Get user by ID
     * 
     * @param id User ID
     * @return User object or null if not found
     */
    public User getUserById(Long id) {
        logger.info("UserService.getUserById() called with ID: {}", id);
        try {
            User user = userRepository.findById(id);
            if (user != null) {
                logger.info("Successfully retrieved user with ID: {}", id);
            } else {
                logger.warn("User not found with ID: {}", id);
            }
            return user;
        } catch (Exception e) {
            logger.error("Error getting user by ID: {}", id, e);
            throw new RuntimeException("Failed to get user by ID: " + id, e);
        }
    }
    
    /**
     * Create a new user
     * 
     * @param user User to create
     * @return Created user
     */
    public User createUser(User user) {
        logger.info("UserService.createUser() called for user: {}", user.getName());
        try {
            User savedUser = userRepository.save(user);
            logger.info("Successfully created user with ID: {}", savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            logger.error("Error creating user: {}", user.getName(), e);
            throw new RuntimeException("Failed to create user: " + user.getName(), e);
        }
    }
    
    /**
     * Update an existing user
     * 
     * @param user User to update
     * @return Updated user
     */
    public User updateUser(User user) {
        logger.info("UserService.updateUser() called for user: {}", user.getName());
        try {
            User updatedUser = userRepository.save(user);
            logger.info("Successfully updated user with ID: {}", updatedUser.getId());
            return updatedUser;
        } catch (Exception e) {
            logger.error("Error updating user: {}", user.getName(), e);
            throw new RuntimeException("Failed to update user: " + user.getName(), e);
        }
    }
    
    /**
     * Delete user by ID
     * 
     * @param id User ID to delete
     */
    public void deleteUser(Long id) {
        logger.info("UserService.deleteUser() called for ID: {}", id);
        try {
            userRepository.deleteById(id);
            logger.info("Successfully deleted user with ID: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting user with ID: {}", id, e);
            throw new RuntimeException("Failed to delete user with ID: " + id, e);
        }
    }
    
    /**
     * Search users by name and email
     * 
     * @param name Name to search for (optional)
     * @param email Email to search for (optional)
     * @return List of matching users
     */
    public List<User> searchUsers(String name, String email) {
        logger.info("UserService.searchUsers() called with name: {}, email: {}", name, email);
        
        try {
            // Simulate search functionality
            List<User> users = new ArrayList<>();
            
            // Add some sample users for demonstration
            users.add(new User(1L, "John Doe", "john.doe@netflix.com"));
            users.add(new User(2L, "Jane Smith", "jane.smith@netflix.com"));
            users.add(new User(3L, "Bob Johnson", "bob.johnson@netflix.com"));
            
            // Filter by name if provided
            if (name != null && !name.trim().isEmpty()) {
                users = users.stream()
                        .filter(user -> user.getName().toLowerCase().contains(name.toLowerCase()))
                        .collect(java.util.stream.Collectors.toList());
            }
            
            // Filter by email if provided
            if (email != null && !email.trim().isEmpty()) {
                users = users.stream()
                        .filter(user -> user.getEmail().toLowerCase().contains(email.toLowerCase()))
                        .collect(java.util.stream.Collectors.toList());
            }
            
            logger.info("Search completed successfully, found {} users", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Error searching users with name: {}, email: {}", name, email, e);
            throw new RuntimeException("Failed to search users", e);
        }
    }
    
    /**
     * Getter for the repository (for testing purposes)
     * 
     * @return The injected UserRepository
     */
    public UserRepository getUserRepository() {
        return userRepository;
    }
}
