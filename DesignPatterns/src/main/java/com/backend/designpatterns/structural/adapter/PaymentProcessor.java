package com.backend.designpatterns.structural.adapter;

// unified interface
public interface PaymentProcessor {
    PaymentResponse processPayment(double amount, String currency);
}
