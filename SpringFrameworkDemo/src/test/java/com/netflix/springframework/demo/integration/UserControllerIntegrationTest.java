package com.netflix.springframework.demo.integration;

import com.netflix.springframework.demo.dto.UserCreateRequest;
import com.netflix.springframework.demo.dto.UserUpdateRequest;
import com.netflix.springframework.demo.entity.UserEntity;
import com.netflix.springframework.demo.repository.UserJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * UserControllerIntegrationTest - Comprehensive Integration Tests
 * 
 * This test class demonstrates Netflix production-grade integration testing:
 * 1. Full Spring Boot context integration tests
 * 2. Database integration with JPA
 * 3. REST API endpoint testing
 * 4. JSON serialization/deserialization testing
 * 5. Error handling and validation testing
 * 
 * For C/C++ engineers:
 * - Integration tests are like end-to-end tests in C++
 * - MockMvc is like HTTP client testing in C++
 * - @SpringBootTest is like full application testing in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private UserJpaRepository userRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private MockMvc mockMvc;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        userRepository.deleteAll();
    }
    
    /**
     * Test create user endpoint
     */
    @Test
    void testCreateUser() throws Exception {
        UserCreateRequest request = new UserCreateRequest();
        request.setName("John Doe");
        request.setEmail("john.doe@netflix.com");
        
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data.name").value("John Doe"))
                .andExpect(jsonPath("$.data.email").value("john.doe@netflix.com"));
    }
    
    /**
     * Test create user with validation error
     */
    @Test
    void testCreateUserValidationError() throws Exception {
        UserCreateRequest request = new UserCreateRequest();
        request.setName(""); // Invalid name
        request.setEmail("invalid-email"); // Invalid email
        
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    /**
     * Test get user by ID
     */
    @Test
    void testGetUserById() throws Exception {
        // Create a user first
        UserEntity user = new UserEntity();
        user.setName("Jane Doe");
        user.setEmail("jane.doe@netflix.com");
        user.setAge(25);
        user = userRepository.save(user);
        
        mockMvc.perform(get("/api/v1/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Jane Doe"))
                .andExpect(jsonPath("$.data.email").value("jane.doe@netflix.com"));
    }
    
    /**
     * Test get user by ID not found
     */
    @Test
    void testGetUserByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"));
    }
    
    /**
     * Test update user
     */
    @Test
    void testUpdateUser() throws Exception {
        // Create a user first
        UserEntity user = new UserEntity();
        user.setName("Bob Smith");
        user.setEmail("bob.smith@netflix.com");
        user.setAge(30);
        user = userRepository.save(user);
        
        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("Bob Johnson");
        request.setEmail("bob.johnson@netflix.com");
        
        mockMvc.perform(put("/api/v1/users/{id}", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User updated successfully"))
                .andExpect(jsonPath("$.data.name").value("Bob Johnson"))
                .andExpect(jsonPath("$.data.email").value("bob.johnson@netflix.com"));
    }
    
    /**
     * Test update user not found
     */
    @Test
    void testUpdateUserNotFound() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("Updated Name");
        
        mockMvc.perform(put("/api/v1/users/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"));
    }
    
    /**
     * Test delete user
     */
    @Test
    void testDeleteUser() throws Exception {
        // Create a user first
        UserEntity user = new UserEntity();
        user.setName("Delete User");
        user.setEmail("delete.user@netflix.com");
        user.setAge(25);
        user = userRepository.save(user);
        
        mockMvc.perform(delete("/api/v1/users/{id}", user.getId()))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }
    
    /**
     * Test delete user not found
     */
    @Test
    void testDeleteUserNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"));
    }
    
    /**
     * Test get all users
     */
    @Test
    void testGetAllUsers() throws Exception {
        // Create multiple users
        UserEntity user1 = new UserEntity();
        user1.setName("User One");
        user1.setEmail("user1@netflix.com");
        user1.setAge(25);
        userRepository.save(user1);
        
        UserEntity user2 = new UserEntity();
        user2.setName("User Two");
        user2.setEmail("user2@netflix.com");
        user2.setAge(30);
        userRepository.save(user2);
        
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))));
    }
    
    /**
     * Test search users
     */
    @Test
    void testSearchUsers() throws Exception {
        // Create users with different names
        UserEntity user1 = new UserEntity();
        user1.setName("John Doe");
        user1.setEmail("john.doe@netflix.com");
        user1.setAge(25);
        userRepository.save(user1);
        
        UserEntity user2 = new UserEntity();
        user2.setName("Jane Smith");
        user2.setEmail("jane.smith@netflix.com");
        user2.setAge(30);
        userRepository.save(user2);
        
        mockMvc.perform(get("/api/v1/users/search")
                .param("name", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    /**
     * Test search users by email
     */
    @Test
    void testSearchUsersByEmail() throws Exception {
        // Create users with different emails
        UserEntity user1 = new UserEntity();
        user1.setName("User One");
        user1.setEmail("user1@netflix.com");
        user1.setAge(25);
        userRepository.save(user1);
        
        UserEntity user2 = new UserEntity();
        user2.setName("User Two");
        user2.setEmail("user2@example.com");
        user2.setAge(30);
        userRepository.save(user2);
        
        mockMvc.perform(get("/api/v1/users/search")
                .param("email", "netflix.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    /**
     * Test invalid request body
     */
    @Test
    void testInvalidRequestBody() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    /**
     * Test invalid path variable
     */
    @Test
    void testInvalidPathVariable() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", "invalid-id"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
