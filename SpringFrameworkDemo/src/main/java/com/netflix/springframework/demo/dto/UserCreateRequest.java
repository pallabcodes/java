package com.netflix.springframework.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * UserCreateRequest - DTO for creating users
 * 
 * This class demonstrates:
 * 1. DTO (Data Transfer Object) pattern
 * 2. JSON serialization/deserialization
 * 3. Request validation
 * 4. Jackson annotations for JSON handling
 * 
 * For C/C++ engineers:
 * - DTOs are like data structures for API communication
 * - Similar to structs in C++ but with JSON serialization
 * - @JsonProperty is like field mapping for JSON
 * - Validation is like input validation in C++
 * 
 * @author Netflix SDE-2 Team
 */
public class UserCreateRequest {
    
    @JsonProperty("name")
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @JsonProperty("email")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    /**
     * Default constructor
     * 
     * Required for JSON deserialization
     * Similar to default constructor in C++
     */
    public UserCreateRequest() {
        System.out.println("UserCreateRequest default constructor called");
    }
    
    /**
     * Parameterized constructor
     * 
     * @param name User name
     * @param email User email
     */
    public UserCreateRequest(String name, String email) {
        this.name = name;
        this.email = email;
        System.out.println("UserCreateRequest parameterized constructor called: " + name);
    }
    
    // Getters and Setters
    // In C++, these would be like getter/setter methods or public member access
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * toString method for debugging and logging
     * 
     * Similar to operator<< in C++ or toString methods
     */
    @Override
    public String toString() {
        return "UserCreateRequest{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
    
    /**
     * equals method for object comparison
     * 
     * Similar to operator== in C++
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        UserCreateRequest that = (UserCreateRequest) obj;
        return name != null ? name.equals(that.name) : that.name == null &&
               email != null ? email.equals(that.email) : that.email == null;
    }
    
    /**
     * hashCode method for hash-based collections
     * 
     * Similar to hash functions in C++
     */
    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        return result;
    }
}
