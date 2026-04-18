package com.backend.designpatterns.creational.builder;

import java.util.List;

public class BuilderDemo {

    public static void main(String[] args) {
        System.out.println("=== L5 Builder Pattern Demo (Java 21 Records) ===");

        // 1. Using the fluent builder to create a complex object
        User user = User.builder("USR-001", "John Doe")
                .email("john.doe@google.com")
                .role("ADMIN")
                .addPermission("READ")
                .addPermission("WRITE")
                .build();

        System.out.println("\n[CREATED USER]");
        System.out.println(user);

        // 2. DEMONSTRATING L5 IMMUTABILITY (Defensive Copying)
        System.out.println("\n--- Testing Immutability & Defensive Copying ---");
        
        List<String> userPermissions = user.permissions();
        System.out.println("Initial Permissions: " + userPermissions);

        try {
            System.out.println("Attempting to modify permissions list directly...");
            userPermissions.add("DELETE"); // This should fail!
        } catch (UnsupportedOperationException e) {
            System.out.println("✅ SUCCESS: Caught expected UnsupportedOperationException.");
            System.out.println("L5 Rationale: The Record correctly returned an Unmodifiable view.");
        }

        // 3. VALIDATION DEMO
        System.out.println("\n--- Testing Strict Validation ---");
        try {
            User.builder("USR-002", "Jane Smith")
                .email("invalid-email-format")
                .build();
        } catch (IllegalArgumentException e) {
            System.out.println("✅ SUCCESS: Caught expected validation error: " + e.getMessage());
        }

        System.out.println("\n[L5 ACHIEVEMENT]: The User object is deeply immutable, " + 
                           "thread-safe, and self-validating.");
    }
}
