package com.backend.designpatterns.creational.builder;

/**
 * 🧠 WHY BUILDER PATTERN ACTUALLY EXISTS (Real Reason, not textbook)
 *
 * Most explanations say: "too many constructor parameters". That's shallow.
 * 
 * The real problem Builder solves:
 * 👉 Controlled construction of a valid object over time.
 * 
 * In real systems, object creation often has:
 * - Many optional inputs (config, flags, metadata)
 * - Cross-field validation (field A affects field B)
 * - Defaults that depend on other fields
 * - Need for readability (especially in configs)
 * - Immutability requirement
 * 
 * 🔥 WITHOUT BUILDER (What goes wrong)
 * ❌ Telescoping constructor hell:
 *    new HttpClient(5000, true, 3, false, null);
 *    Problems: Unreadable, order-dependent, easy to break, validation scattered.
 * 
 * ❌ Setters (Even worse):
 *    HttpClient c = new HttpClient();
 *    c.setTimeout(5000);
 *    c.setRetry(true);
 *    Problems: Object is temporarily invalid, not thread-safe, mutation bugs.
 * 
 * ✅ BUILDER GIVES YOU:
 *    HttpClient client = HttpClient.builder()
 *        .timeout(5000)
 *        .retry(3)
 *        .enableLogging()
 *        .build();
 * 
 * What’s REALLY happening:
 * You’re creating a controlled staging area → then producing a fully valid immutable object.
 * 
 * 💡 MENTAL MODEL:
 * Builder = Mutable assembly phase
 * Object = Immutable final product
 * 
 * ---
 * ⚠️ WHEN IS BUILDER OVERKILL?
 * - 2-3 fields only
 * - All fields are required
 * - No complex validation
 * - Object is short-lived (e.g., DTO, request object)
 * 
 * In these cases, prefer:
 * - Constructors
 * - Static factory methods
 */



// Immutable Domain Object (here we don't use or instantiate the actual class i.e. User but user a Builder class to build the object)
// Role: Concrete Product
public final class User {

    private final String id;
    private final String name;
    private final String email;
    private final String role;
    private final boolean active;

    // private constrcutor so we prevent direct instantiation of User class even though it can be within this User class itself (which is what we did within build method)
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
        private String email; // it has implicit default value of null
        private String role = "USER";
        private boolean active = true;

        // Constructor for Builder class (only required fields i.e. id, name)
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
