package com.backend.designpatterns.structural.adapter;

// Third party SDK
public class StripeGateway {

    public StripeResponse pay(double amount, String currency) {

        // Simulate success
        return new StripeResponse(
                "pay_123",
                amount,
                currency,
                "succeeded"
        );
    }
}
