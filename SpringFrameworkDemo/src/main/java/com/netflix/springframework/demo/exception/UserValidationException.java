package com.netflix.springframework.demo.exception;

import java.util.List;
import java.util.ArrayList;

/**
 * UserValidationException - Custom Exception for User Validation Errors
 * 
 * This exception demonstrates Netflix production-grade validation error handling:
 * 1. Custom exception with validation error details
 * 2. Collection of validation errors
 * 3. Proper error codes and messages
 * 4. Serializable for proper exception handling
 * 
 * For C/C++ engineers:
 * - Custom exceptions are like custom error classes in C++
 * - Validation errors are like error collections in C++
 * - Error codes are like error enums in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
public class UserValidationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_MESSAGE = "User validation failed";
    private static final String ERROR_CODE = "USER_VALIDATION_FAILED";
    
    private final String errorCode;
    private final List<ValidationError> validationErrors;
    
    /**
     * Default constructor
     */
    public UserValidationException() {
        super(DEFAULT_MESSAGE);
        this.errorCode = ERROR_CODE;
        this.validationErrors = new ArrayList<>();
    }
    
    /**
     * Constructor with custom message
     * 
     * @param message Custom error message
     */
    public UserValidationException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
        this.validationErrors = new ArrayList<>();
    }
    
    /**
     * Constructor with validation errors
     * 
     * @param validationErrors List of validation errors
     */
    public UserValidationException(List<ValidationError> validationErrors) {
        super(DEFAULT_MESSAGE);
        this.errorCode = ERROR_CODE;
        this.validationErrors = validationErrors != null ? validationErrors : new ArrayList<>();
    }
    
    /**
     * Constructor with message and validation errors
     * 
     * @param message Custom error message
     * @param validationErrors List of validation errors
     */
    public UserValidationException(String message, List<ValidationError> validationErrors) {
        super(message);
        this.errorCode = ERROR_CODE;
        this.validationErrors = validationErrors != null ? validationErrors : new ArrayList<>();
    }
    
    /**
     * Constructor with cause
     * 
     * @param cause The cause of the exception
     */
    public UserValidationException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause);
        this.errorCode = ERROR_CODE;
        this.validationErrors = new ArrayList<>();
    }
    
    /**
     * Constructor with message and cause
     * 
     * @param message Custom error message
     * @param cause The cause of the exception
     */
    public UserValidationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ERROR_CODE;
        this.validationErrors = new ArrayList<>();
    }
    
    /**
     * Add validation error
     * 
     * @param field Field name
     * @param message Error message
     */
    public void addValidationError(String field, String message) {
        this.validationErrors.add(new ValidationError(field, message));
    }
    
    /**
     * Add validation error with code
     * 
     * @param field Field name
     * @param message Error message
     * @param code Error code
     */
    public void addValidationError(String field, String message, String code) {
        this.validationErrors.add(new ValidationError(field, message, code));
    }
    
    /**
     * Get error code
     * 
     * @return Error code
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Get validation errors
     * 
     * @return List of validation errors
     */
    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }
    
    /**
     * Check if there are validation errors
     * 
     * @return true if there are validation errors
     */
    public boolean hasValidationErrors() {
        return !validationErrors.isEmpty();
    }
    
    /**
     * Get number of validation errors
     * 
     * @return Number of validation errors
     */
    public int getValidationErrorCount() {
        return validationErrors.size();
    }
    
    /**
     * Validation error class
     */
    public static class ValidationError {
        private final String field;
        private final String message;
        private final String code;
        
        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
            this.code = null;
        }
        
        public ValidationError(String field, String message, String code) {
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
        public String toString() {
            return String.format("ValidationError{field='%s', message='%s', code='%s'}", 
                               field, message, code);
        }
    }
}
