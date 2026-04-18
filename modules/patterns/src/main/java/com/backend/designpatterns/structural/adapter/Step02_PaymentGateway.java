package com.backend.designpatterns.structural.adapter;

/**
 * Step 2: TARGET INTERFACE
 */
public interface Step02_PaymentGateway {
    Step01_PaymentResponse processPayment(Step01_PaymentRequest request);
}
