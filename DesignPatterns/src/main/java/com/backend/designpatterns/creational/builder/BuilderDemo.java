package com.backend.designpatterns.creational.builder;

public class BuilderDemo {

    public static void main(String[] args) {
        
        System.out.println("--- Builder Pattern Demo ---");

        // Use Case: Use Builder when you have many optional fields, immutable objects, 
        // or need to perform validation during object construction.

        try {
            // 1. Valid User
            User user = new User.Builder("U101", "Alice")
                    .email("alice@example.com")
                    .role("ADMIN")
                    .build();
            System.out.println("Created: " + user);

            // 2. Invalid User (Validation Check)
            User invalid = new User.Builder("U102", "Bob")
                    .email("invalid-email") 
                    .build();
            
        } catch (IllegalStateException e) {
            System.out.println("Validation Error: " + e.getMessage());
        }
    }
}
