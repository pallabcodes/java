package com.backend.designpatterns.creational.builder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 🧠 L5 BUILDER PATTERN (Modern Java 21 Record Edition)
 *
 * Why this is L5+:
 * 1. 100% IMMUTABILITY: Uses Java 'record' which is final and deeply immutable.
 * 2. DEFENSIVE COPYING: Ensures internal collections (permissions) cannot be modified from outside.
 * 3. AUDITABILITY: Automatically sets 'createdAt' during build.
 * 4. STRICT VALIDATION: Validates business rules before object birth.
 */
public record User(
    String id,
    String name,
    String email,
    String role,
    boolean isActive,
    List<String> permissions,
    Instant createdAt
) {
    /**
     * Compact constructor for the record to ensure the permissions list is unmodifiable.
     * This is a "L5 Defensive Copy" pattern.
     */
    public User {
        Objects.requireNonNull(id, "ID cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(permissions, "Permissions list cannot be null");
        // Ensure deep immutability by wrapping as unmodifiable
        permissions = Collections.unmodifiableList(new ArrayList<>(permissions));
    }

    public static Builder builder(String id, String name) {
        return new Builder(id, name);
    }

    /**
     * Static Inner Builder
     * Provides a fluent 'Staging Area' before creating the immutable Record.
     */
    public static class Builder {
        private final String id;
        private final String name;
        private String email;
        private String role = "USER";
        private boolean isActive = true;
        private final List<String> permissions = new ArrayList<>();

        public Builder(String id, String name) {
            this.id = Objects.requireNonNull(id, "ID is mandatory");
            this.name = Objects.requireNonNull(name, "Name is mandatory");
        }

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
         * The 'build' call pulls the trigger.
         */
        public User build() {
            // Business Validation
            if (email != null && !email.contains("@")) {
                throw new IllegalArgumentException("Invalid email format: " + email);
            }

            // Automatic field injection (Auditability)
            Instant createdAt = Instant.now();

            return new User(id, name, email, role, isActive, permissions, createdAt);
        }
    }
}
