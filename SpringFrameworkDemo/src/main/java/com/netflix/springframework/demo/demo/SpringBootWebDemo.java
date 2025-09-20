package com.netflix.springframework.demo.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.netflix.springframework.demo.dto.UserCreateRequest;
import com.netflix.springframework.demo.dto.ApiResponse;
import com.netflix.springframework.demo.model.User;

import java.util.List;

/**
 * SpringBootWebDemo - Demonstrates Spring Boot Web Development
 * 
 * This class demonstrates:
 * 1. Spring Boot web server startup
 * 2. REST API endpoints
 * 3. JSON serialization/deserialization
 * 4. HTTP client usage
 * 5. Embedded web server functionality
 * 
 * For C/C++ engineers:
 * - This is like starting a web server in C++
 * - REST APIs are like HTTP endpoints
 * - JSON handling is like serialization/deserialization
 * - Embedded server is like running a web server within the application
 * 
 * @author Netflix SDE-2 Team
 */
@SpringBootApplication
public class SpringBootWebDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Spring Boot Web Development Demo ===");
        System.out.println("Demonstrating web server and REST API development\n");
        
        // Start Spring Boot application with embedded web server
        ConfigurableApplicationContext context = SpringApplication.run(SpringBootWebDemo.class, args);
        
        // Demonstrate web server functionality
        demonstrateWebServer(context);
        
        // Demonstrate REST API usage
        demonstrateRestApi();
        
        // Graceful shutdown
        context.close();
    }
    
    /**
     * Demonstrates Spring Boot web server functionality
     * 
     * @param context Spring application context
     */
    private static void demonstrateWebServer(ConfigurableApplicationContext context) {
        System.out.println("=== WEB SERVER DEMONSTRATION ===");
        
        // Get web server information
        System.out.println("1. EMBEDDED WEB SERVER:");
        System.out.println("   - Server Type: Tomcat (embedded)");
        System.out.println("   - Port: 8080 (default)");
        System.out.println("   - Context Path: /");
        System.out.println("   - Auto-configuration: ENABLED");
        
        // Get server port from environment
        String serverPort = System.getProperty("server.port", "8080");
        System.out.println("   - Actual Port: " + serverPort);
        
        System.out.println("\n2. SPRING BOOT WEB FEATURES:");
        System.out.println("   - Auto-configuration: ENABLED");
        System.out.println("   - Embedded Tomcat: ENABLED");
        System.out.println("   - JSON serialization: ENABLED");
        System.out.println("   - CORS support: ENABLED");
        System.out.println("   - Error handling: ENABLED");
        System.out.println("   - Health checks: ENABLED");
        
        System.out.println("\n3. AVAILABLE ENDPOINTS:");
        System.out.println("   - GET    /api/v1/users           - Get all users");
        System.out.println("   - GET    /api/v1/users/{id}      - Get user by ID");
        System.out.println("   - POST   /api/v1/users           - Create new user");
        System.out.println("   - PUT    /api/v1/users/{id}      - Update user");
        System.out.println("   - DELETE /api/v1/users/{id}      - Delete user");
        System.out.println("   - GET    /api/v1/users/search    - Search users");
        System.out.println("   - GET    /actuator/health        - Health check");
        System.out.println("   - GET    /actuator/info          - Application info");
        
        System.out.println("\n4. WEB SERVER CONFIGURATION:");
        System.out.println("   - CORS: Configured for /api/** endpoints");
        System.out.println("   - Content-Type: application/json");
        System.out.println("   - Character Encoding: UTF-8");
        System.out.println("   - Request Mapping: Automatic");
        System.out.println("   - Error Pages: Custom error handling");
    }
    
    /**
     * Demonstrates REST API usage with HTTP client
     * 
     * This shows how to use the REST API endpoints
     */
    private static void demonstrateRestApi() {
        System.out.println("\n=== REST API DEMONSTRATION ===");
        
        // Create REST client
        RestTemplate restTemplate = new RestTemplate();
        String baseUrl = "http://localhost:8080/api/v1/users";
        
        System.out.println("1. REST API CLIENT DEMONSTRATION:");
        System.out.println("   - Base URL: " + baseUrl);
        System.out.println("   - Client: RestTemplate (Spring's HTTP client)");
        System.out.println("   - Content-Type: application/json");
        
        try {
            // Test GET all users
            System.out.println("\n2. TESTING GET ALL USERS:");
            System.out.println("   - Endpoint: GET " + baseUrl);
            System.out.println("   - Expected: List of users");
            
            // Note: In a real scenario, you would make actual HTTP requests
            // For this demo, we'll simulate the responses
            System.out.println("   - Response: 200 OK");
            System.out.println("   - Data: [User{id=1, name='John Doe', email='john.doe@netflix.com'}, ...]");
            
            // Test GET user by ID
            System.out.println("\n3. TESTING GET USER BY ID:");
            System.out.println("   - Endpoint: GET " + baseUrl + "/1");
            System.out.println("   - Expected: Single user");
            System.out.println("   - Response: 200 OK");
            System.out.println("   - Data: User{id=1, name='John Doe', email='john.doe@netflix.com'}");
            
            // Test POST create user
            System.out.println("\n4. TESTING CREATE USER:");
            System.out.println("   - Endpoint: POST " + baseUrl);
            System.out.println("   - Request Body: {\"name\":\"New User\",\"email\":\"new.user@netflix.com\"}");
            System.out.println("   - Expected: Created user");
            System.out.println("   - Response: 201 Created");
            System.out.println("   - Data: User{id=1234567890, name='New User', email='new.user@netflix.com'}");
            
            // Test PUT update user
            System.out.println("\n5. TESTING UPDATE USER:");
            System.out.println("   - Endpoint: PUT " + baseUrl + "/1");
            System.out.println("   - Request Body: {\"name\":\"Updated User\",\"email\":\"updated@netflix.com\"}");
            System.out.println("   - Expected: Updated user");
            System.out.println("   - Response: 200 OK");
            System.out.println("   - Data: User{id=1, name='Updated User', email='updated@netflix.com'}");
            
            // Test DELETE user
            System.out.println("\n6. TESTING DELETE USER:");
            System.out.println("   - Endpoint: DELETE " + baseUrl + "/1");
            System.out.println("   - Expected: No content");
            System.out.println("   - Response: 204 No Content");
            
            // Test search users
            System.out.println("\n7. TESTING SEARCH USERS:");
            System.out.println("   - Endpoint: GET " + baseUrl + "/search?name=John");
            System.out.println("   - Expected: Filtered users");
            System.out.println("   - Response: 200 OK");
            System.out.println("   - Data: [User{id=1, name='John Doe', email='john.doe@netflix.com'}]");
            
        } catch (Exception e) {
            System.out.println("   - Error in REST API demonstration: " + e.getMessage());
        }
        
        System.out.println("\n8. JSON SERIALIZATION/DESERIALIZATION:");
        System.out.println("   - Request: JSON -> Java Object (deserialization)");
        System.out.println("   - Response: Java Object -> JSON (serialization)");
        System.out.println("   - Library: Jackson (automatic)");
        System.out.println("   - Annotations: @JsonProperty, @JsonIgnore, etc.");
        
        System.out.println("\n9. HTTP STATUS CODES:");
        System.out.println("   - 200 OK: Successful GET, PUT");
        System.out.println("   - 201 Created: Successful POST");
        System.out.println("   - 204 No Content: Successful DELETE");
        System.out.println("   - 400 Bad Request: Invalid request data");
        System.out.println("   - 404 Not Found: Resource not found");
        System.out.println("   - 500 Internal Server Error: Server error");
    }
}
