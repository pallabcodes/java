package com.netflix.springframework.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

/**
 * UserUpdateRequest - DTO for updating users
 * 
 * This class demonstrates:
 * 1. DTO pattern for update operations
 * 2. Optional fields for partial updates
 * 3. JSON serialization/deserialization
 * 4. PATCH-like update semantics
 * 
 * For C/C++ engineers:
 * - Update DTOs often have optional fields
 * - Similar to partial update structures in C++
 * - Allows for flexible update operations
 * - JSON deserialization handles null values gracefully
 * 
 * @author Netflix SDE-2 Team
 */
public class UserUpdateRequest {
    
    @JsonProperty("name")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @JsonProperty("email")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    /**
     * Default constructor
     * 
     * Required for JSON deserialization
     * Similar to default constructor in C++
     */
    public UserUpdateRequest() {
        System.out.println("UserUpdateRequest default constructor called");
    }
    
    /**
     * Parameterized constructor
     * 
     * @param name User name (optional)
     * @param email User email (optional)
     */
    public UserUpdateRequest(String name, String email) {
        this.name = name;
        this.email = email;
        System.out.println("UserUpdateRequest parameterized constructor called");
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
     * Check if name is provided
     * 
     * @return true if name is not null and not empty
     */
    public boolean hasName() {
        return name != null && !name.trim().isEmpty();
    }
    
    /**
     * Check if email is provided
     * 
     * @return true if email is not null and not empty
     */
    public boolean hasEmail() {
        return email != null && !email.trim().isEmpty();
    }
    
    /**
     * Check if any field is provided
     * 
     * @return true if at least one field is provided
     */
    public boolean hasAnyField() {
        return hasName() || hasEmail();
    }
    
    /**
     * toString method for debugging and logging
     * 
     * Similar to operator<< in C++ or toString methods
     */
    @Override
    public String toString() {
        return "UserUpdateRequest{" +
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
        
        UserUpdateRequest that = (UserUpdateRequest) obj;
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
