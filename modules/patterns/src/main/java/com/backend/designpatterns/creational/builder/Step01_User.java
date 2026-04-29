package com.backend.designpatterns.creational.builder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Step 1: THE TARGET ENTITY (with Inner Builder)
 * 
 * Problem: How do we create a complex object with many fields without making a messy constructor?
 * Solution: The Builder Pattern.
 * 
 * Why use it?
 * 1. Readability: You can see exactly what field is being set (e.g., .email("..."))
 * 2. Mandatory vs Optional: Forces mandatory fields in the constructor, makes others optional.
 * 3. Immutability: The Builder constructs the object, but the final object (User) is a Record (Immutable).
 * 4. Validation: We can validate the entire object at once in the .build() method.
 */
public record Step01_User(
    String id,
    String name,
    String email,
    String role,
    boolean isActive,
    List<String> permissions,
    Instant createdAt
) {
    public Step01_User {
        Objects.requireNonNull(id, "ID cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(permissions, "Permissions list cannot be null");
        permissions = Collections.unmodifiableList(new ArrayList<>(permissions));
    }

    public static Builder builder(String id, String name) {
        return new Builder(id, name);
    }

    /**
     * THE BUILDER (The "Construction Worker")
     * 
     * This class acts as a temporary scratchpad to hold values until 
     * we are ready to create the final Step01_User object.
     */
    public static class Builder {
        private final String id;
        private final String name;
        private String email;
        private String role = "USER";
        private boolean isActive = true;
        private final List<String> permissions = new ArrayList<>();

        /**
         * Mandatory fields are passed here.
         * You can't even start building a User without an ID and Name.
         */
        public Builder(String id, String name) {
            this.id = Objects.requireNonNull(id, "ID is mandatory");
            this.name = Objects.requireNonNull(name, "Name is mandatory");
        }

        /**
         * Fluent methods:
         * We return 'this' (the builder itself) so you can chain calls like:
         * .email("...").role("...").active(true)
         */
        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder active(boolean active) {
            this.isActive = active;
            return this;
        }

        public Builder addPermission(String permission) {
            this.permissions.add(permission);
            return this;
        }

        /**
         * THE FINAL STEP
         * 1. Validates that the email is actually an email.
         * 2. Sets the creation timestamp.
         * 3. Finally creates the immutable User object.
         */
        public Step01_User build() {
            if (email != null && !email.contains("@")) {
                throw new IllegalArgumentException("Invalid email format: " + email);
            }
            Instant createdAt = Instant.now();
            return new Step01_User(id, name, email, role, isActive, permissions, createdAt);
        }
    }
}
