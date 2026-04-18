package com.backend.designpatterns.structural.adapter;

/**
 * Step 3: THE ADAPTEE (Legacy SDK)
 */
public class Step03_StripeLegacySdk {

    public String executeCharge(String apiKey, String account, String amountCents, String idempotency) 
                                throws Step03_StripeConnectionException {
        
        System.out.println("[Stripe SDK] Authenticating with key...");
        System.out.println("[Stripe SDK] Charging account '" + account + "' for " + amountCents + " cents.");

        if ("ACT-FAIL".equals(account)) {
            throw new Step03_StripeConnectionException("Connection reset by peer at stripe.com");
        }

        if ("ACT-DECLINE".equals(account)) {
            return "DECLINED|null|Insufficient Funds";
        }

        return "SUCCESS|STRIPE-TXN-98765|null";
    }
}
