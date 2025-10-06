package com.netflix.productivity.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class OverloadControllerAdvice {

    public static class OverloadedException extends RuntimeException {
        public OverloadedException(String message) {
            super(message);
        }
    }

    @ExceptionHandler(OverloadedException.class)
    public ResponseEntity<String> handleOverload(OverloadedException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Service temporarily overloaded");
    }
}
