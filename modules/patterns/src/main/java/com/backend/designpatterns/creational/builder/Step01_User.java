package com.backend.designpatterns.creational.builder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Step 1: THE TARGET ENTITY (with Inner Builder)
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

        public Step01_User build() {
            if (email != null && !email.contains("@")) {
                throw new IllegalArgumentException("Invalid email format: " + email);
            }
            Instant createdAt = Instant.now();
            return new Step01_User(id, name, email, role, isActive, permissions, createdAt);
        }
    }
}
