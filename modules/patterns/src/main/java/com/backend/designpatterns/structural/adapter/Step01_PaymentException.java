package com.backend.designpatterns.structural.adapter;

/**
 * Step 1: DOMAIN MODELS (Exceptions)
 */
public class Step01_PaymentException extends RuntimeException {
    public Step01_PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
