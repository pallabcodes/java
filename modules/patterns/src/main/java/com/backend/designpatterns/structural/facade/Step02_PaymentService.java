package com.backend.designpatterns.structural.facade;

/**
 * Step 2: SUBSYSTEM (Payment)
 */
public class Step02_PaymentService {

    public boolean chargeCreditCard(String cardNumber, double amount) {
        System.out.println("[PaymentService] Initiating charge of $" + amount + " on card " + maskCard(cardNumber));
        
        // Simulate a declined card for testing a facade rollback
        if (cardNumber.endsWith("0000")) {
            System.err.println("[PaymentService] ❌ Transaction Declined! Insufficient funds.");
            return false;
        }

        System.out.println("[PaymentService] ✅ Transaction Successful.");
        return true;
    }

    private String maskCard(String card) {
        return "****-****-****-" + card.substring(Math.max(0, card.length() - 4));
    }
}
