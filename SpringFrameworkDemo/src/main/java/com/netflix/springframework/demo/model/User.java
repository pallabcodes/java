package com.netflix.springframework.demo.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * User Model Class
 * 
 * This is a simple POJO (Plain Old Java Object) that represents a user entity.
 * 
 * For C/C++ engineers:
 * - This is similar to a struct or class in C/C++
 * - POJOs are simple data containers without business logic
 * - Similar to data structures you might define in C/C++
 * 
 * @author Netflix SDE-2 Team
 */
public class User {
    
    @NotNull(message = "ID is required")
    @Positive(message = "ID must be positive")
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    /**
     * Default constructor
     * 
     * Spring and many frameworks require a default constructor
     * Similar to default constructors in C++
     */
    public User() {
        System.out.println("User default constructor called");
    }
    
    /**
     * Parameterized constructor
     * 
     * @param id User ID
     * @param name User name
     * @param email User email
     */
    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        System.out.println("User parameterized constructor called: " + name);
    }
    
    // Getters and Setters
    // In C++, these would be similar to getter/setter methods or public member access
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
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
        
        User user = (User) obj;
        return id != null ? id.equals(user.id) : user.id == null;
    }
    
    /**
     * hashCode method for hash-based collections
     * 
     * Similar to hash functions in C++
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
