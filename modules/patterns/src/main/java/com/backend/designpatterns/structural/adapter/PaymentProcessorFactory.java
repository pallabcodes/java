package com.backend.designpatterns.structural.adapter;

// Factory Pattern: Handles the instantiation logic
public class PaymentProcessorFactory {

    public static PaymentProcessor getProcessor(String provider) {
        return switch (provider.toLowerCase()) {
            case "stripe" -> new StripeAdapter(new StripeGateway());
            case "paypal" -> new PayPalAdapter(new PayPalGateway());
            default -> throw new RuntimeException("Unsupported payment provider: " + provider);
        };
    }
}
