package com.algorithmpractice.solid;

import java.util.Objects;

/**
 * Request object for creating a new user.
 * 
 * <p>This class demonstrates Single Responsibility Principle by only
 * containing the data needed for user creation. It includes validation
 * and provides a clean, immutable interface for user creation requests.</p>
 * 
 * <p>Key benefits:</p>
 * <ul>
 *   <li><strong>Immutable</strong>: Once created, cannot be modified</li>
 *   <li><strong>Validated</strong>: Includes input validation</li>
 *   <li><strong>Focused</strong>: Only contains user creation data</li>
 *   <li><strong>Type-safe</strong>: Uses proper types for all fields</li>
 * </ul>
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public final class CreateUserRequest {

    private final String email;
    private final String firstName;
    private final String lastName;
    private final String password;
    private final String phoneNumber;

    /**
     * Constructs a new CreateUserRequest with the specified data.
     * 
     * @param email the user's email address (required)
     * @param firstName the user's first name (required)
     * @param lastName the user's last name (required)
     * @param password the user's password (required)
     * @param phoneNumber the user's phone number (optional)
     * @throws IllegalArgumentException if required fields are null or empty
     */
    public CreateUserRequest(
            final String email,
            final String firstName,
            final String lastName,
            final String password,
            final String phoneNumber) {
        
        // Validate required fields
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        // Validate email format
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Email must contain @ symbol");
        }

        // Validate password strength
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        // Store validated data
        this.email = email.trim().toLowerCase();
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.password = password;
        this.phoneNumber = phoneNumber != null ? phoneNumber.trim() : null;
    }

    /**
     * Gets the user's email address.
     * 
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the user's first name.
     * 
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Gets the user's last name.
     * 
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Gets the user's password.
     * 
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the user's phone number.
     * 
     * @return the phone number, or null if not provided
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Checks if a phone number was provided.
     * 
     * @return true if phone number is present, false otherwise
     */
    public boolean hasPhoneNumber() {
        return phoneNumber != null && !phoneNumber.trim().isEmpty();
    }

    /**
     * Gets the user's full name.
     * 
     * @return the concatenated first and last name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final CreateUserRequest other = (CreateUserRequest) obj;
        return Objects.equals(email, other.email) &&
               Objects.equals(firstName, other.firstName) &&
               Objects.equals(lastName, other.lastName) &&
               Objects.equals(password, other.password) &&
               Objects.equals(phoneNumber, other.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, firstName, lastName, password, phoneNumber);
    }

    @Override
    public String toString() {
        return "CreateUserRequest{" +
                "email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", password='[HIDDEN]'" +
                '}';
    }

    /**
     * Builder class for creating CreateUserRequest instances.
     * 
     * <p>This builder provides a fluent API for creating user creation requests
     * with proper validation and optional fields.</p>
     */
    public static class Builder {
        private String email;
        private String firstName;
        private String lastName;
        private String password;
        private String phoneNumber;

        public Builder email(final String email) {
            this.email = email;
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

        public Builder password(final String password) {
            this.password = password;
            return this;
        }

        public Builder phoneNumber(final String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public CreateUserRequest build() {
            return new CreateUserRequest(email, firstName, lastName, password, phoneNumber);
        }
    }
}
