package com.netflix.productivity.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final ResponseMapper responseMapper;

    public GlobalExceptionHandler(ResponseMapper responseMapper) {
        this.responseMapper = responseMapper;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return responseMapper.badRequest(ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors, ErrorCodes.VALIDATION_ERROR));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(v -> errors.put(v.getPropertyPath().toString(), v.getMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors, ErrorCodes.VALIDATION_ERROR));
    }

    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        return responseMapper.unauthorized("Invalid credentials", ErrorCodes.AUTH_INVALID_CREDENTIALS);
    }

    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<ApiResponse<Void>> handleAuth(AuthenticationException ex) {
        return responseMapper.unauthorized("Authentication required", ErrorCodes.AUTH_REQUIRED);
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return responseMapper.forbidden("Forbidden", ErrorCodes.AUTH_FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        // expose errorCode for the metrics interceptor
        org.springframework.web.context.request.RequestAttributes attrs = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            attrs.setAttribute("errorCode", ErrorCodes.INTERNAL_ERROR, org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST);
        }
        return responseMapper.serverError("Unexpected error", null);
    }
}


