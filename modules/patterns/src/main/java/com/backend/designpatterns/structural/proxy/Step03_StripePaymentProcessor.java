package com.backend.designpatterns.structural.proxy;

/**
 * Step 3: REAL SUBJECT (Payment)
 */
public class Step03_StripePaymentProcessor implements Step02_PaymentProcessor {
    @Override
    public boolean process(String account, double amount) {
        System.out.println("[STRIPE] Processing payment of $" + amount + " for " + account);
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return true;
    }
}
