package com.backend.designpatterns.structural.adapter;

import java.util.Map;

// Service / Facade using Strategy & Factory Patterns
public class PaymentService {

    private final TransactionLogger logger = new TransactionLogger();

    public PaymentResponse pay(String provider, double amount, String currency) {
        
        // Facade Step 1: Logging (Subsystem 1)
        logger.log("Initiating payment via " + provider);

        // Facade Step 2: Validation (Simple Logic)
        if (amount <= 0) {
            throw new RuntimeException("Invalid amount");
        }

        // Facade Step 3: Get Strategy (Subsystem 2 - Factory)
        PaymentProcessor processor = PaymentProcessorFactory.getProcessor(provider);

        // Facade Step 4: Execute (Subsystem 3 - Adapter)
        PaymentResponse response = processor.processPayment(amount, currency);

        // Facade Step 5: Log Completion
        logger.log("Payment completed: " + response.getTransactionId());

        return response;
    }
}
