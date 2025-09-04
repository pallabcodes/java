package com.algorithmpractice.solid;

import java.util.Objects;

/**
 * User entity representing a system user.
 * 
 * <p>This class demonstrates several SOLID principles:</p>
 * <ul>
 *   <li><strong>Single Responsibility</strong>: Only manages user data and state</li>
 *   <li><strong>Open/Closed</strong>: Can be extended through inheritance without modifying existing code</li>
 *   <li><strong>Liskov Substitution</strong>: Subclasses can be used in place of this class</li>
 * </ul>
 * 
 * <p>The class uses the Builder pattern for easy object creation and provides
 * immutable behavior where appropriate. It also includes proper validation
 * and business logic encapsulation.</p>
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public final class User {

    // Immutable fields - once set, cannot be changed
    private final String id;
    private final String email;
    private final long createdAt;

    // Mutable fields - can be updated during user lifecycle
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserStatus status;
    private long updatedAt;

    /**
     * Private constructor to enforce use of Builder pattern.
     * 
     * @param builder the builder containing user data
     */
    private User(final Builder builder) {
        this.id = builder.id;
        this.email = builder.email;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    /**
     * Public constructor for creating User instances directly.
     * 
     * @param id the user ID
     * @param email the user email
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param status the user status
     * @param createdAt the creation timestamp
     * @param updatedAt the last update timestamp
     */
    public User(final String id, final String email, final String firstName, final String lastName, 
                final UserStatus status, final long createdAt, final long updatedAt) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters for immutable fields
    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    // Getters for mutable fields
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public UserStatus getStatus() {
        return status;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the updated timestamp.
     * 
     * @param updatedAt the new updated timestamp
     */
    public void setUpdatedAt(final long updatedAt) {
        if (updatedAt <= 0) {
            throw new IllegalArgumentException("Updated at timestamp must be positive");
        }
        this.updatedAt = updatedAt;
    }

    // Setters for mutable fields with validation
    public void setFirstName(final String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        this.firstName = firstName.trim();
        this.updatedAt = System.currentTimeMillis();
    }

    public void setLastName(final String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
        this.lastName = lastName.trim();
        this.updatedAt = System.currentTimeMillis();
    }

    public void setPhoneNumber(final String phoneNumber) {
        // Phone number can be null (optional field)
        if (phoneNumber != null && phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty if provided");
        }
        this.phoneNumber = phoneNumber != null ? phoneNumber.trim() : null;
        this.updatedAt = System.currentTimeMillis();
    }

    public void setStatus(final UserStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * Deactivates the user account.
     * 
     * <p>This method demonstrates business logic encapsulation by ensuring
     * that status changes follow business rules and are properly tracked.</p>
     */
    public void deactivate() {
        if (this.status == UserStatus.DEACTIVATED) {
            throw new IllegalStateException("User is already deactivated");
        }
        this.status = UserStatus.DEACTIVATED;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * Activates the user account.
     * 
     * <p>This method allows reactivation of previously deactivated accounts,
     * following business rules for user lifecycle management.</p>
     */
    public void activate() {
        if (this.status == UserStatus.ACTIVE) {
            throw new IllegalStateException("User is already active");
        }
        this.status = UserStatus.ACTIVE;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * Gets the user's full name.
     * 
     * @return the concatenated first and last name
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return "Unknown User";
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    /**
     * Checks if the user is active.
     * 
     * @return true if the user status is ACTIVE, false otherwise
     */
    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }

    /**
     * Checks if the user is deactivated.
     * 
     * @return true if the user status is DEACTIVATED, false otherwise
     */
    public boolean isDeactivated() {
        return UserStatus.DEACTIVATED.equals(this.status);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    /**
     * Builder class for creating User instances.
     * 
     * <p>This builder pattern provides a fluent API for creating User objects
     * with proper validation and default values.</p>
     */
    public static class Builder {
        private String id;
        private String email;
        private String firstName;
        private String lastName;
        private UserStatus status = UserStatus.ACTIVE;
        private long createdAt = System.currentTimeMillis();
        private long updatedAt = System.currentTimeMillis();

        public Builder id(final String id) {
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("ID cannot be null or empty");
            }
            this.id = id.trim();
            return this;
        }

        public Builder email(final String email) {
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Email cannot be null or empty");
            }
            if (!email.contains("@")) {
                throw new IllegalArgumentException("Email must contain @ symbol");
            }
            this.email = email.trim().toLowerCase();
            return this;
        }

        public Builder firstName(final String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(final String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder status(final UserStatus status) {
            if (status == null) {
                throw new IllegalArgumentException("Status cannot be null");
            }
            this.status = status;
            return this;
        }

        public Builder createdAt(final long createdAt) {
            if (createdAt <= 0) {
                throw new IllegalArgumentException("Created at timestamp must be positive");
            }
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(final long updatedAt) {
            if (updatedAt <= 0) {
                throw new IllegalArgumentException("Updated at timestamp must be positive");
            }
            this.updatedAt = updatedAt;
            return this;
        }

        public User build() {
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalStateException("ID is required");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalStateException("Email is required");
            }
            return new User(this);
        }
    }
}
