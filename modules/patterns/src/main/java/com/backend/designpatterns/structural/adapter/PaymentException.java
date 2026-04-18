package com.backend.designpatterns.structural.adapter;

// unified exception
public class PaymentException extends RuntimeException {

    private final String code;

    public PaymentException(String code) {
        super(code);
        this.code = code;
    }

    public PaymentException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
