package com.backend.designpatterns.structural.proxy;

public class StripePaymentProcessor implements PaymentProcessor {
    @Override
    public boolean process(String account, double amount) {
        System.out.println("[STRIPE] Processing payment of $" + amount + " for " + account);
        try {
            // Simulate network latency
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return true;
    }
}
