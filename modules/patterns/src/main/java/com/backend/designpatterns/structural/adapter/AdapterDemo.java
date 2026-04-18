package com.backend.designpatterns.structural.adapter;

import java.util.UUID;

public class AdapterDemo {

    public static void main(String[] args) {
        System.out.println("=== L5 Adapter Pattern Demo (Anti-Corruption Layer) ===\n");

        // setup
        Step03_StripeLegacySdk stripeSdk = new Step03_StripeLegacySdk();
        Step02_PaymentGateway paymentGateway = new Step04_StripePaymentAdapter(stripeSdk, "sk_live_12345");

        // Scenario A
        System.out.println("--- Scenario A: Successful Payment ---");
        Step01_PaymentRequest successReq = new Step01_PaymentRequest("ACT-123", 45.50, UUID.randomUUID().toString());
        Step01_PaymentResponse successRes = paymentGateway.processPayment(successReq);
        System.out.println("App Result: " + successRes + "\n");

        // Scenario B
        System.out.println("--- Scenario B: Declined Payment ---");
        Step01_PaymentRequest declineReq = new Step01_PaymentRequest("ACT-DECLINE", 100.00, UUID.randomUUID().toString());
        Step01_PaymentResponse declineRes = paymentGateway.processPayment(declineReq);
        System.out.println("App Result: " + declineRes + "\n");

        // Scenario C
        System.out.println("--- Scenario C: Infrastructure Failure ---");
        Step01_PaymentRequest failReq = new Step01_PaymentRequest("ACT-FAIL", 50.00, UUID.randomUUID().toString());
        
        try {
            paymentGateway.processPayment(failReq);
        } catch (Step01_PaymentException e) {
            System.err.println("App Caught Clean Exception: " + e.getClass().getSimpleName() + 
                               " -> " + e.getMessage());
        }
    }
}
