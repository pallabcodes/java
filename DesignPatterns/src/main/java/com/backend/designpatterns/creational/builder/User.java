package com.backend.designpatterns.creational.builder;

// Immutable Domain Object
// Role: Concrete Product
public final class User {

    private final String id;
    private final String name;
    private final String email;
    private final String role;
    private final boolean active;

    private User(Builder b) {
        this.id = b.id;
        this.name = b.name;
        this.email = b.email;
        this.role = b.role;
        this.active = b.active;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }

    @Override
    public String toString() {
        return String.format("User{id='%s', name='%s', email='%s', role='%s', active=%s}",
                id, name, email, role, active);
    }

    // Static Inner Builder
    // Role: Builder
    public static class Builder {

        // Required
        private final String id;
        private final String name;

        // Optional with defaults
        private String email;
        private String role = "USER";
        private boolean active = true;

        public Builder(String id, String name) {
            this.id = id;
            this.name = name;
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
            this.active = active;
            return this;
        }

        public User build() {
            // Production-grade validation
            if (id == null || id.isBlank())
                throw new IllegalStateException("ID required");
            
            if (name == null || name.isBlank())
                throw new IllegalStateException("Name required");

            if (email != null && !email.contains("@"))
                throw new IllegalStateException("Invalid email format");

            return new User(this);
        }
    }
}
