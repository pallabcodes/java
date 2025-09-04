package com.algorithmpractice.solid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Result object for validation operations.
 * 
 * <p>This class demonstrates Single Responsibility Principle by only
 * containing validation result data. It provides a clean, immutable
 * interface for validation results and supports multiple validation errors.</p>
 * 
 * <p>Key benefits:</p>
 * <ul>
 *   <li><strong>Immutable</strong>: Once created, cannot be modified</li>
 *   <li><strong>Comprehensive</strong>: Supports multiple validation errors</li>
 *   <li><strong>Type-safe</strong>: Uses proper types for all fields</li>
 *   <li><strong>Easy to use</strong>: Simple boolean check for validity</li>
 * </ul>
 * 
 * @author Algorithm Practice Team
 * @version 1.0.0
 */
public final class ValidationResult {

    private final boolean valid;
    private final List<String> errorMessages;
    private final List<ValidationError> validationErrors;

    /**
     * Constructs a new ValidationResult.
     * 
     * @param valid whether the validation passed
     * @param errorMessages list of error messages
     * @param validationErrors list of detailed validation errors
     */
    private ValidationResult(
            final boolean valid,
            final List<String> errorMessages,
            final List<ValidationError> validationErrors) {
        
        this.valid = valid;
        this.errorMessages = errorMessages != null ? 
            new ArrayList<>(errorMessages) : new ArrayList<>();
        this.validationErrors = validationErrors != null ? 
            new ArrayList<>(validationErrors) : new ArrayList<>();
    }

    /**
     * Creates a successful validation result.
     * 
     * @return a ValidationResult indicating successful validation
     */
    public static ValidationResult success() {
        return new ValidationResult(true, null, null);
    }

    /**
     * Creates a failed validation result with a single error message.
     * 
     * @param errorMessage the error message
     * @return a ValidationResult indicating failed validation
     */
    public static ValidationResult failure(final String errorMessage) {
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("Error message cannot be null or empty");
        }
        
        final List<String> messages = new ArrayList<>();
        messages.add(errorMessage.trim());
        
        return new ValidationResult(false, messages, null);
    }

    /**
     * Creates a failed validation result with multiple error messages.
     * 
     * @param errorMessages the list of error messages
     * @return a ValidationResult indicating failed validation
     */
    public static ValidationResult failure(final List<String> errorMessages) {
        if (errorMessages == null || errorMessages.isEmpty()) {
            throw new IllegalArgumentException("Error messages cannot be null or empty");
        }
        
        final List<String> messages = new ArrayList<>();
        for (final String message : errorMessages) {
            if (message != null && !message.trim().isEmpty()) {
                messages.add(message.trim());
            }
        }
        
        if (messages.isEmpty()) {
            throw new IllegalArgumentException("At least one valid error message is required");
        }
        
        return new ValidationResult(false, messages, null);
    }

    /**
     * Creates a failed validation result with detailed validation errors.
     * 
     * @param validationErrors the list of validation errors
     * @return a ValidationResult indicating failed validation
     */
    public static ValidationResult failureWithDetails(final List<ValidationError> validationErrors) {
        if (validationErrors == null || validationErrors.isEmpty()) {
            throw new IllegalArgumentException("Validation errors cannot be null or empty");
        }
        
        final List<String> messages = new ArrayList<>();
        for (final ValidationError error : validationErrors) {
            if (error != null && error.getMessage() != null) {
                messages.add(error.getMessage());
            }
        }
        
        return new ValidationResult(false, messages, validationErrors);
    }

    /**
     * Checks if the validation passed.
     * 
     * @return true if validation was successful, false otherwise
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Checks if the validation failed.
     * 
     * @return true if validation failed, false otherwise
     */
    public boolean isInvalid() {
        return !valid;
    }

    /**
     * Gets the list of error messages.
     * 
     * @return an unmodifiable list of error messages
     */
    public List<String> getErrorMessages() {
        return Collections.unmodifiableList(errorMessages);
    }

    /**
     * Gets the list of detailed validation errors.
     * 
     * @return an unmodifiable list of validation errors
     */
    public List<ValidationError> getValidationErrors() {
        return Collections.unmodifiableList(validationErrors);
    }

    /**
     * Gets the first error message.
     * 
     * @return the first error message, or null if no errors
     */
    public String getErrorMessage() {
        return errorMessages.isEmpty() ? null : errorMessages.get(0);
    }

    /**
     * Gets the number of error messages.
     * 
     * @return the count of error messages
     */
    public int getErrorCount() {
        return errorMessages.size();
    }

    /**
     * Checks if there are any error messages.
     * 
     * @return true if there are error messages, false otherwise
     */
    public boolean hasErrors() {
        return !errorMessages.isEmpty();
    }

    /**
     * Combines this validation result with another.
     * 
     * <p>If either result is invalid, the combined result will be invalid
     * and contain all error messages from both results.</p>
     * 
     * @param other the other validation result to combine with
     * @return a new ValidationResult combining both results
     */
    public ValidationResult combine(final ValidationResult other) {
        if (other == null) {
            return this;
        }
        
        if (this.valid && other.valid) {
            return ValidationResult.success();
        }
        
        final List<String> combinedMessages = new ArrayList<>(this.errorMessages);
        combinedMessages.addAll(other.errorMessages);
        
        final List<ValidationError> combinedErrors = new ArrayList<>(this.validationErrors);
        combinedErrors.addAll(other.validationErrors);
        
        return new ValidationResult(false, combinedMessages, combinedErrors);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ValidationResult other = (ValidationResult) obj;
        return valid == other.valid &&
               Objects.equals(errorMessages, other.errorMessages) &&
               Objects.equals(validationErrors, other.validationErrors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valid, errorMessages, validationErrors);
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "valid=" + valid +
                ", errorMessages=" + errorMessages +
                ", validationErrors=" + validationErrors +
                '}';
    }

    /**
     * Detailed validation error containing field and message information.
     */
    public static final class ValidationError {
        private final String field;
        private final String message;
        private final String code;

        public ValidationError(final String field, final String message, final String code) {
            this.field = field;
            this.message = message;
            this.code = code;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }

        public String getCode() {
            return code;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final ValidationError other = (ValidationError) obj;
            return Objects.equals(field, other.field) &&
                   Objects.equals(message, other.message) &&
                   Objects.equals(code, other.code);
        }

        @Override
        public int hashCode() {
            return Objects.hash(field, message, code);
        }

        @Override
        public String toString() {
            return "ValidationError{" +
                    "field='" + field + '\'' +
                    ", message='" + message + '\'' +
                    ", code='" + code + '\'' +
                    '}';
        }
    }
}
