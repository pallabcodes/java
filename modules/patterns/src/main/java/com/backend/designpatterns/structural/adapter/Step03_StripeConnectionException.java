package com.backend.designpatterns.structural.adapter;

/**
 * Step 3: ADAPTEE EXCEPTIONS
 */
public class Step03_StripeConnectionException extends Exception {
    public Step03_StripeConnectionException(String message) {
        super(message);
    }
}
