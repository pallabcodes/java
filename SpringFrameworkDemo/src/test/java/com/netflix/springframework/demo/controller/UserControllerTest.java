package com.netflix.springframework.demo.controller;

import com.netflix.springframework.demo.dto.UserCreateRequest;
import com.netflix.springframework.demo.dto.UserUpdateRequest;
import com.netflix.springframework.demo.dto.ApiResponse;
import com.netflix.springframework.demo.model.User;
import com.netflix.springframework.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest - Demonstrates Spring Boot Web Testing
 * 
 * This test class demonstrates:
 * 1. Spring Boot web testing with @WebMvcTest
 * 2. MockMvc for testing REST endpoints
 * 3. JSON serialization/deserialization testing
 * 4. HTTP method testing (GET, POST, PUT, DELETE)
 * 5. Mocking service dependencies
 * 
 * For C/C++ engineers:
 * - This is like testing web endpoints in C++
 * - MockMvc is like a mock web server for testing
 * - @WebMvcTest is like testing only the web layer
 * - Mocking is like stubbing dependencies in C++
 * 
 * @author Netflix SDE-2 Team
 */
@WebMvcTest(UserController.class)
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Test GET /api/v1/users - Get all users
     * 
     * This demonstrates:
     * - Testing GET endpoints
     * - JSON response validation
     * - HTTP status code validation
     * - Mock service behavior
     */
    @Test
    void testGetAllUsers() throws Exception {
        // Arrange
        List<User> users = new ArrayList<>();
        users.add(new User(1L, "John Doe", "john.doe@netflix.com"));
        users.add(new User(2L, "Jane Smith", "jane.smith@netflix.com"));
        
        when(userService.searchUsers(null, null)).thenReturn(users);
        
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Users retrieved successfully"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].name").value("John Doe"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].name").value("Jane Smith"));
    }
    
    /**
     * Test GET /api/v1/users/{id} - Get user by ID
     * 
     * This demonstrates:
     * - Testing GET with path variables
     * - Testing successful response
     * - Testing not found response
     */
    @Test
    void testGetUserById() throws Exception {
        // Arrange
        User user = new User(1L, "John Doe", "john.doe@netflix.com");
        when(userService.getUserById(1L)).thenReturn(user);
        when(userService.getUserById(999L)).thenReturn(null);
        
        // Act & Assert - Success case
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value("John Doe"));
        
        // Act & Assert - Not found case
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User not found"));
    }
    
    /**
     * Test POST /api/v1/users - Create new user
     * 
     * This demonstrates:
     * - Testing POST endpoints
     * - JSON request body handling
     * - Request validation
     * - HTTP 201 Created status
     */
    @Test
    void testCreateUser() throws Exception {
        // Arrange
        UserCreateRequest request = new UserCreateRequest("New User", "new.user@netflix.com");
        User createdUser = new User(1L, "New User", "new.user@netflix.com");
        
        when(userService.createUser(any(User.class))).thenReturn(createdUser);
        
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User created successfully"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value("New User"));
    }
    
    /**
     * Test POST /api/v1/users - Create user with validation error
     * 
     * This demonstrates:
     * - Testing validation errors
     * - HTTP 400 Bad Request status
     * - Error response handling
     */
    @Test
    void testCreateUserValidationError() throws Exception {
        // Arrange
        UserCreateRequest request = new UserCreateRequest("", "invalid-email");
        
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Name is required"));
    }
    
    /**
     * Test PUT /api/v1/users/{id} - Update user
     * 
     * This demonstrates:
     * - Testing PUT endpoints
     * - Path variable and request body
     * - Update operations
     */
    @Test
    void testUpdateUser() throws Exception {
        // Arrange
        UserUpdateRequest request = new UserUpdateRequest("Updated User", "updated@netflix.com");
        User existingUser = new User(1L, "John Doe", "john.doe@netflix.com");
        User updatedUser = new User(1L, "Updated User", "updated@netflix.com");
        
        when(userService.getUserById(1L)).thenReturn(existingUser);
        when(userService.updateUser(any(User.class))).thenReturn(updatedUser);
        
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User updated successfully"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value("Updated User"));
    }
    
    /**
     * Test DELETE /api/v1/users/{id} - Delete user
     * 
     * This demonstrates:
     * - Testing DELETE endpoints
     * - HTTP 204 No Content status
     * - Delete operations
     */
    @Test
    void testDeleteUser() throws Exception {
        // Arrange
        User existingUser = new User(1L, "John Doe", "john.doe@netflix.com");
        when(userService.getUserById(1L)).thenReturn(existingUser);
        
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User deleted successfully"));
    }
    
    /**
     * Test GET /api/v1/users/search - Search users
     * 
     * This demonstrates:
     * - Testing GET with query parameters
     * - Search functionality
     * - Optional parameters
     */
    @Test
    void testSearchUsers() throws Exception {
        // Arrange
        List<User> users = new ArrayList<>();
        users.add(new User(1L, "John Doe", "john.doe@netflix.com"));
        
        when(userService.searchUsers("John", null)).thenReturn(users);
        
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/search")
                .param("name", "John")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Search completed successfully"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].name").value("John Doe"));
    }
    
    /**
     * Test JSON serialization/deserialization
     * 
     * This demonstrates:
     * - JSON conversion testing
     * - Object mapping validation
     * - Serialization/deserialization
     */
    @Test
    void testJsonSerialization() throws Exception {
        // Arrange
        User user = new User(1L, "John Doe", "john.doe@netflix.com");
        String json = objectMapper.writeValueAsString(user);
        
        // Act
        User deserializedUser = objectMapper.readValue(json, User.class);
        
        // Assert
        assert deserializedUser.getId().equals(user.getId());
        assert deserializedUser.getName().equals(user.getName());
        assert deserializedUser.getEmail().equals(user.getEmail());
    }
}
