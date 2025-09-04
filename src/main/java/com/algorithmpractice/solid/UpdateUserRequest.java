package com.algorithmpractice.solid;

import java.util.Objects;

/**
 * Request object for updating an existing user.
 * 
 * <p>This class demonstrates Single Responsibility Principle by only
 * containing the data needed for user updates. It allows partial updates
 * where only specified fields are modified, making it flexible for
 * different update scenarios.</p>
 * 
 * <p>Key benefits:</p>
 * <ul>
 *   <li><strong>Immutable</strong>: Once created, cannot be modified</li>
 *   <li><strong>Flexible</strong>: Supports partial updates</li>
 *   <li><strong>Validated</strong>: Includes input validation</li>
 *   <li><strong>Type-safe</strong>: Uses proper types for all fields</li>
 * </ul>
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public final class UpdateUserRequest {

    private final String firstName;
    private final String lastName;
    private final String phoneNumber;
    private final UserStatus status;

    /**
     * Constructs a new UpdateUserRequest with the specified data.
     * 
     * <p>All fields are optional, allowing for partial updates. Only
     * the fields that are not null will be updated in the user profile.</p>
     * 
     * @param firstName the user's new first name (optional)
     * @param lastName the user's new last name (optional)
     * @param phoneNumber the user's new phone number (optional)
     * @param status the user's new status (optional)
     * @throws IllegalArgumentException if provided fields are invalid
     */
    public UpdateUserRequest(
            final String firstName,
            final String lastName,
            final String phoneNumber,
            final UserStatus status) {
        
        // Validate firstName if provided
        if (firstName != null && firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty if provided");
        }
        this.firstName = firstName != null ? firstName.trim() : null;

        // Validate lastName if provided
        if (lastName != null && lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty if provided");
        }
        this.lastName = lastName != null ? lastName.trim() : null;

        // Validate phoneNumber if provided
        if (phoneNumber != null && phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty if provided");
        }
        this.phoneNumber = phoneNumber != null ? phoneNumber.trim() : null;

        // Status is already validated by the enum
        this.status = status;
    }

    /**
     * Gets the new first name.
     * 
     * @return the first name, or null if not being updated
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Gets the new last name.
     * 
     * @return the last name, or null if not being updated
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Gets the new phone number.
     * 
     * @return the phone number, or null if not being updated
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Gets the new status.
     * 
     * @return the status, or null if not being updated
     */
    public UserStatus getStatus() {
        return status;
    }

    /**
     * Checks if the first name is being updated.
     * 
     * @return true if firstName is provided, false otherwise
     */
    public boolean isFirstNameUpdated() {
        return firstName != null;
    }

    /**
     * Checks if the last name is being updated.
     * 
     * @return true if lastName is provided, false otherwise
     */
    public boolean isLastNameUpdated() {
        return lastName != null;
    }

    /**
     * Checks if the phone number is being updated.
     * 
     * @return true if phoneNumber is provided, false otherwise
     */
    public boolean isPhoneNumberUpdated() {
        return phoneNumber != null;
    }

    /**
     * Checks if the status is being updated.
     * 
     * @return true if status is provided, false otherwise
     */
    public boolean isStatusUpdated() {
        return status != null;
    }

    /**
     * Checks if any fields are being updated.
     * 
     * @return true if at least one field is provided, false otherwise
     */
    public boolean hasUpdates() {
        return isFirstNameUpdated() || isLastNameUpdated() || 
               isPhoneNumberUpdated() || isStatusUpdated();
    }

    /**
     * Gets the number of fields being updated.
     * 
     * @return the count of fields that are not null
     */
    public int getUpdateCount() {
        int count = 0;
        if (isFirstNameUpdated()) count++;
        if (isLastNameUpdated()) count++;
        if (isPhoneNumberUpdated()) count++;
        if (isStatusUpdated()) count++;
        return count;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final UpdateUserRequest other = (UpdateUserRequest) obj;
        return Objects.equals(firstName, other.firstName) &&
               Objects.equals(lastName, other.lastName) &&
               Objects.equals(phoneNumber, other.phoneNumber) &&
               Objects.equals(status, other.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, phoneNumber, status);
    }

    @Override
    public String toString() {
        return "UpdateUserRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", status=" + status +
                '}';
    }

    /**
     * Builder class for creating UpdateUserRequest instances.
     * 
     * <p>This builder provides a fluent API for creating user update requests
     * with only the fields that need to be updated.</p>
     */
    public static class Builder {
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private UserStatus status;

        public Builder firstName(final String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(final String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder phoneNumber(final String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder status(final UserStatus status) {
            this.status = status;
            return this;
        }

        public UpdateUserRequest build() {
            return new UpdateUserRequest(firstName, lastName, phoneNumber, status);
        }
    }
}
