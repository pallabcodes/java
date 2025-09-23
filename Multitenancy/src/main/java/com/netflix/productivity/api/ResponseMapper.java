package com.netflix.productivity.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ResponseMapper {

    public <T> ResponseEntity<ApiResponse<T>> ok(T body) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "OK", body));
    }

    public <T> ResponseEntity<ApiResponse<T>> created(T body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Created", body));
    }

    public <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), message, null));
    }

    public <T> ResponseEntity<ApiResponse<T>> badRequest(String message, Object error) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), message, error));
    }

    public <T> ResponseEntity<ApiResponse<T>> serverError(String message, Object error) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), message, error));
    }
}


