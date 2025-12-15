package com.netflix.streaming.infrastructure.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Global exception handler for consistent error responses across all services.
 * 
 * Provides:
 * - Consistent error response format
 * - Proper HTTP status codes
 * - Error correlation IDs for tracing
 * - Security-conscious error messages
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        String errorId = UUID.randomUUID().toString();
        logger.warn("Validation failed [errorId={}, path={}]", errorId, request.getDescription(false));

        ErrorResponse error = ErrorResponse.builder()
                .errorId(errorId)
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Request validation failed")
                .path(request.getDescription(false).replace("uri=", ""))
                .details(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        String errorId = UUID.randomUUID().toString();
        logger.warn("Illegal argument [errorId={}, message={}]", errorId, ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .errorId(errorId)
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {
        
        String errorId = UUID.randomUUID().toString();
        logger.warn("Illegal state [errorId={}, message={}]", errorId, ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .errorId(errorId)
                .timestamp(Instant.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        String errorId = UUID.randomUUID().toString();
        logger.debug("Resource not found [errorId={}, resource={}]", errorId, ex.getResourceType());

        ErrorResponse error = ErrorResponse.builder()
                .errorId(errorId)
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(EventPublishingException.class)
    public ResponseEntity<ErrorResponse> handleEventPublishingException(
            EventPublishingException ex, WebRequest request) {
        
        String errorId = UUID.randomUUID().toString();
        logger.error("Event publishing failed [errorId={}]", errorId, ex);

        ErrorResponse error = ErrorResponse.builder()
                .errorId(errorId)
                .timestamp(Instant.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Event Publishing Failed")
                .message("Failed to publish event. Please retry.")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        
        String errorId = UUID.randomUUID().toString();
        logger.error("Unexpected error [errorId={}]", errorId, ex);

        ErrorResponse error = ErrorResponse.builder()
                .errorId(errorId)
                .timestamp(Instant.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please contact support with error ID: " + errorId)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Standardized error response structure
     */
    public static class ErrorResponse {
        private String errorId;
        private Instant timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private Map<String, Object> details;

        private ErrorResponse(Builder builder) {
            this.errorId = builder.errorId;
            this.timestamp = builder.timestamp;
            this.status = builder.status;
            this.error = builder.error;
            this.message = builder.message;
            this.path = builder.path;
            this.details = builder.details;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String errorId;
            private Instant timestamp;
            private int status;
            private String error;
            private String message;
            private String path;
            private Map<String, Object> details;

            public Builder errorId(String errorId) {
                this.errorId = errorId;
                return this;
            }

            public Builder timestamp(Instant timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public Builder status(int status) {
                this.status = status;
                return this;
            }

            public Builder error(String error) {
                this.error = error;
                return this;
            }

            public Builder message(String message) {
                this.message = message;
                return this;
            }

            public Builder path(String path) {
                this.path = path;
                return this;
            }

            public Builder details(Map<String, Object> details) {
                this.details = details;
                return this;
            }

            public ErrorResponse build() {
                return new ErrorResponse(this);
            }
        }

        // Getters
        public String getErrorId() { return errorId; }
        public Instant getTimestamp() { return timestamp; }
        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
        public String getPath() { return path; }
        public Map<String, Object> getDetails() { return details; }
    }
}

