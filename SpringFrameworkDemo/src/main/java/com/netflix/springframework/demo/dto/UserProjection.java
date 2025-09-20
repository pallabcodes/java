package com.netflix.springframework.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * UserProjection - Data Transfer Object for User Projections
 * 
 * This DTO demonstrates Netflix production-grade data transfer object implementation:
 * 1. Projection interface for performance optimization
 * 2. JSON serialization with custom field mapping
 * 3. Validation annotations for data integrity
 * 4. Immutable design pattern
 * 5. Builder pattern for object creation
 * 
 * For C/C++ engineers:
 * - DTOs are like data structures in C++
 * - Projections are like selecting specific fields in SQL
 * - JSON mapping is like serialization in C++
 * - Builder pattern is like factory methods in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
public class UserProjection {
    
    @NotNull(message = "ID is required")
    @JsonProperty("id")
    private final Long id;
    
    @NotNull(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @JsonProperty("name")
    private final String name;
    
    @NotNull(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @JsonProperty("email")
    private final String email;
    
    /**
     * Constructor for projection
     * 
     * @param id User ID
     * @param name User name
     * @param email User email
     */
    public UserProjection(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    /**
     * Get user ID
     * 
     * @return User ID
     */
    public Long getId() {
        return id;
    }
    
    /**
     * Get user name
     * 
     * @return User name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get user email
     * 
     * @return User email
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * Get display name
     * 
     * @return Formatted display name
     */
    public String getDisplayName() {
        return String.format("%s (%s)", name, email);
    }
    
    /**
     * equals method for projection comparison
     * 
     * @param obj Object to compare
     * @return true if objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        UserProjection that = (UserProjection) obj;
        return java.util.Objects.equals(id, that.id) &&
               java.util.Objects.equals(name, that.name) &&
               java.util.Objects.equals(email, that.email);
    }
    
    /**
     * hashCode method for projection hashing
     * 
     * @return hash code
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, name, email);
    }
    
    /**
     * toString method for debugging
     * 
     * @return string representation
     */
    @Override
    public String toString() {
        return "UserProjection{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
