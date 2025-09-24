package com.netflix.productivity.api;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ResponseMapper {

    public <T> ResponseEntity<ApiResponse<T>> ok(T body) {
        return ResponseEntity.ok(ApiResponse.successWith(HttpStatus.OK.value(), "OK", body, null, currentCorrelationId()));
    }

    public <T> ResponseEntity<ApiResponse<T>> created(T body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.successWith(HttpStatus.CREATED.value(), "Created", body, null, currentCorrelationId()));
    }

    public <T> ResponseEntity<ApiResponse<T>> noContent() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.successWith(HttpStatus.NO_CONTENT.value(), "No Content", null, null, currentCorrelationId()));
    }

    public <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.errorWith(HttpStatus.NOT_FOUND.value(), message, null, "NOT_FOUND", currentCorrelationId()));
    }

    public <T> ResponseEntity<ApiResponse<T>> badRequest(String message, Object error) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.errorWith(HttpStatus.BAD_REQUEST.value(), message, error, "BAD_REQUEST", currentCorrelationId()));
    }

    public <T> ResponseEntity<ApiResponse<T>> unauthorized(String message, String errorCode) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.errorWith(HttpStatus.UNAUTHORIZED.value(), message, null, errorCode, currentCorrelationId()));
    }

    public <T> ResponseEntity<ApiResponse<T>> forbidden(String message, String errorCode) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.errorWith(HttpStatus.FORBIDDEN.value(), message, null, errorCode, currentCorrelationId()));
    }

    public <T> ResponseEntity<ApiResponse<T>> serverError(String message, Object error) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.errorWith(HttpStatus.INTERNAL_SERVER_ERROR.value(), message, error, "INTERNAL_ERROR", currentCorrelationId()));
    }

    private String currentCorrelationId() {
        return MDC.get("correlationId");
    }
}


