package com.netflix.springframework.demo.exception;

/**
 * UserNotFoundException - Custom Exception for User Not Found
 * 
 * This exception demonstrates Netflix production-grade exception handling:
 * 1. Custom exception with proper inheritance
 * 2. Multiple constructors for different scenarios
 * 3. Proper error codes and messages
 * 4. Serializable for proper exception handling
 * 
 * For C/C++ engineers:
 * - Custom exceptions are like custom error classes in C++
 * - Exception inheritance is like class inheritance
 * - Error codes are like error enums in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
public class UserNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_MESSAGE = "User not found";
    private static final String ERROR_CODE = "USER_NOT_FOUND";
    
    private final String errorCode;
    private final Long userId;
    
    /**
     * Default constructor
     */
    public UserNotFoundException() {
        super(DEFAULT_MESSAGE);
        this.errorCode = ERROR_CODE;
        this.userId = null;
    }
    
    /**
     * Constructor with custom message
     * 
     * @param message Custom error message
     */
    public UserNotFoundException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
        this.userId = null;
    }
    
    /**
     * Constructor with user ID
     * 
     * @param userId User ID that was not found
     */
    public UserNotFoundException(Long userId) {
        super(String.format("User not found with ID: %d", userId));
        this.errorCode = ERROR_CODE;
        this.userId = userId;
    }
    
    /**
     * Constructor with user ID and custom message
     * 
     * @param userId User ID that was not found
     * @param message Custom error message
     */
    public UserNotFoundException(Long userId, String message) {
        super(String.format("User not found with ID: %d - %s", userId, message));
        this.errorCode = ERROR_CODE;
        this.userId = userId;
    }
    
    /**
     * Constructor with cause
     * 
     * @param cause The cause of the exception
     */
    public UserNotFoundException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause);
        this.errorCode = ERROR_CODE;
        this.userId = null;
    }
    
    /**
     * Constructor with message and cause
     * 
     * @param message Custom error message
     * @param cause The cause of the exception
     */
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ERROR_CODE;
        this.userId = null;
    }
    
    /**
     * Constructor with user ID and cause
     * 
     * @param userId User ID that was not found
     * @param cause The cause of the exception
     */
    public UserNotFoundException(Long userId, Throwable cause) {
        super(String.format("User not found with ID: %d", userId), cause);
        this.errorCode = ERROR_CODE;
        this.userId = userId;
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
     * Get user ID
     * 
     * @return User ID that was not found
     */
    public Long getUserId() {
        return userId;
    }
    
    /**
     * Check if user ID is available
     * 
     * @return true if user ID is available
     */
    public boolean hasUserId() {
        return userId != null;
    }
}
