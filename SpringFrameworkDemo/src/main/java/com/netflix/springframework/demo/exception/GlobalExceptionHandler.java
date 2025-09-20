package com.netflix.springframework.demo.exception;

import com.netflix.springframework.demo.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler - Comprehensive Exception Handling
 * 
 * This class demonstrates Netflix production-grade exception handling:
 * 1. Global exception handling with @RestControllerAdvice
 * 2. Specific exception handlers for different error types
 * 3. Proper HTTP status codes and error responses
 * 4. Structured error logging and monitoring
 * 5. Security-conscious error messages
 * 
 * For C/C++ engineers:
 * - Global exception handlers are like centralized error handling in C++
 * - @RestControllerAdvice is like a global error handler
 * - Exception mapping is like error code mapping in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String INTERNAL_ERROR_MESSAGE = "An internal error occurred";
    
    /**
     * Handle UserNotFoundException
     * 
     * @param ex The exception
     * @param request The web request
     * @return ResponseEntity with error response
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(
            UserNotFoundException ex, WebRequest request) {
        
        logger.warn("User not found: {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * Handle UserValidationException
     * 
     * @param ex The exception
     * @param request The web request
     * @return ResponseEntity with error response
     */
    @ExceptionHandler(UserValidationException.class)
    public ResponseEntity<ApiResponse<List<ValidationErrorDetail>>> handleUserValidationException(
            UserValidationException ex, WebRequest request) {
        
        logger.warn("User validation failed: {}", ex.getMessage());
        
        List<ValidationErrorDetail> errors = ex.getValidationErrors().stream()
                .map(error -> new ValidationErrorDetail(
                    error.getField(),
                    error.getMessage(),
                    error.getCode()
                ))
                .collect(Collectors.toList());
        
        ApiResponse<List<ValidationErrorDetail>> response = ApiResponse.error(
            "Validation failed", errors);
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Handle MethodArgumentNotValidException (Bean Validation)
     * 
     * @param ex The exception
     * @param request The web request
     * @return ResponseEntity with error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<ValidationErrorDetail>>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        logger.warn("Method argument validation failed: {}", ex.getMessage());
        
        List<ValidationErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ValidationErrorDetail(
                    error.getField(),
                    error.getDefaultMessage(),
                    error.getCode()
                ))
                .collect(Collectors.toList());
        
        ApiResponse<List<ValidationErrorDetail>> response = ApiResponse.error(
            "Validation failed", errors);
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Handle ConstraintViolationException (Bean Validation)
     * 
     * @param ex The exception
     * @param request The web request
     * @return ResponseEntity with error response
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<List<ValidationErrorDetail>>> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        
        logger.warn("Constraint validation failed: {}", ex.getMessage());
        
        List<ValidationErrorDetail> errors = ex.getConstraintViolations().stream()
                .map(violation -> new ValidationErrorDetail(
                    getFieldName(violation.getPropertyPath().toString()),
                    violation.getMessage(),
                    violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName()
                ))
                .collect(Collectors.toList());
        
        ApiResponse<List<ValidationErrorDetail>> response = ApiResponse.error(
            "Validation failed", errors);
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Handle BindException
     * 
     * @param ex The exception
     * @param request The web request
     * @return ResponseEntity with error response
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<List<ValidationErrorDetail>>> handleBindException(
            BindException ex, WebRequest request) {
        
        logger.warn("Binding validation failed: {}", ex.getMessage());
        
        List<ValidationErrorDetail> errors = ex.getFieldErrors().stream()
                .map(error -> new ValidationErrorDetail(
                    error.getField(),
                    error.getDefaultMessage(),
                    error.getCode()
                ))
                .collect(Collectors.toList());
        
        ApiResponse<List<ValidationErrorDetail>> response = ApiResponse.error(
            "Validation failed", errors);
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Handle MethodArgumentTypeMismatchException
     * 
     * @param ex The exception
     * @param request The web request
     * @return ResponseEntity with error response
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        
        logger.warn("Method argument type mismatch: {}", ex.getMessage());
        
        String message = String.format("Invalid value '%s' for parameter '%s'", 
                                     ex.getValue(), ex.getName());
        ApiResponse<Void> response = ApiResponse.error(message);
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Handle HttpMessageNotReadableException
     * 
     * @param ex The exception
     * @param request The web request
     * @return ResponseEntity with error response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {
        
        logger.warn("HTTP message not readable: {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error("Invalid request body");
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Handle IllegalArgumentException
     * 
     * @param ex The exception
     * @param request The web request
     * @return ResponseEntity with error response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        logger.warn("Illegal argument: {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Handle IllegalStateException
     * 
     * @param ex The exception
     * @param request The web request
     * @return ResponseEntity with error response
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {
        
        logger.warn("Illegal state: {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    /**
     * Handle RuntimeException
     * 
     * @param ex The exception
     * @param request The web request
     * @return ResponseEntity with error response
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        logger.error("Runtime exception occurred", ex);
        
        ApiResponse<Void> response = ApiResponse.error(INTERNAL_ERROR_MESSAGE);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * Handle all other exceptions
     * 
     * @param ex The exception
     * @param request The web request
     * @return ResponseEntity with error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, WebRequest request) {
        
        logger.error("Unexpected exception occurred", ex);
        
        ApiResponse<Void> response = ApiResponse.error(INTERNAL_ERROR_MESSAGE);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * Extract field name from property path
     * 
     * @param propertyPath The property path
     * @return Field name
     */
    private String getFieldName(String propertyPath) {
        if (propertyPath == null || propertyPath.isEmpty()) {
            return "unknown";
        }
        
        String[] parts = propertyPath.split("\\.");
        return parts[parts.length - 1];
    }
    
    /**
     * Validation error detail class
     */
    public static class ValidationErrorDetail {
        private final String field;
        private final String message;
        private final String code;
        private final LocalDateTime timestamp;
        
        public ValidationErrorDetail(String field, String message, String code) {
            this.field = field;
            this.message = message;
            this.code = code;
            this.timestamp = LocalDateTime.now();
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
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        @Override
        public String toString() {
            return "ValidationErrorDetail{" +
                    "field='" + field + '\'' +
                    ", message='" + message + '\'' +
                    ", code='" + code + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
}
