package com.backend.designpatterns.structural.adapter;

// Exception DTO
public class StripeException extends RuntimeException {

    private final String code;

    public StripeException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
