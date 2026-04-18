package com.backend.designpatterns.structural.adapter;

// Exception DTO
public class PayPalException extends RuntimeException {

    private final String code;

    public PayPalException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
