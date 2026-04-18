package com.backend.designpatterns.structural.adapter;

/**
 * Step 4: THE ADAPTER (Bridge)
 */
public class Step04_StripePaymentAdapter implements Step02_PaymentGateway {

    private final Step03_StripeLegacySdk stripeSdk;
    private final String apiKey;

    public Step04_StripePaymentAdapter(Step03_StripeLegacySdk stripeSdk, String apiKey) {
        this.stripeSdk = stripeSdk;
        this.apiKey = apiKey;
    }

    @Override
    public Step01_PaymentResponse processPayment(Step01_PaymentRequest request) {
        String amountInCents = String.valueOf((long) (request.amountInUsd() * 100));
        
        System.out.println("[Adapter] Translating to Stripe...");

        String rawResponse;
        try {
            rawResponse = stripeSdk.executeCharge(
                apiKey, 
                request.accountId(), 
                amountInCents, 
                request.idempotencyKey()
            );
        } catch (Step03_StripeConnectionException e) {
            System.err.println("[Adapter] Caught Stripe exception.");
            throw new Step01_PaymentException("Payment provider is currently unavailable.", e);
        }

        System.out.println("[Adapter] Parsing response: " + rawResponse);
        String[] parts = rawResponse.split("\\|");
        
        boolean isSuccess = "SUCCESS".equals(parts[0]);
        String transactionId = "null".equals(parts[1]) ? null : parts[1];
        String failureReason = "null".equals(parts[2]) ? null : parts[2];

        return new Step01_PaymentResponse(transactionId, isSuccess, failureReason);
    }
}
