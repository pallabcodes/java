package com.backend.solid;

/**
 * Step 05: Dependency Inversion Principle (DIP)
 * 
 * L5 Principles:
 * 1. Abstraction First: High-level modules should depend on abstractions, not concrete implementations.
 * 2. Inversion of Control: The caller defines the abstraction, and the implementation adheres to it.
 * 3. Decoupling: Changes to a specific payment provider (Stripe/Paypal) shouldn't affect the 'CheckoutService'.
 */
public class Step05_DIP {

    // 🏆 The Abstraction (Interface)
    public interface PaymentGateway {
        void processPayment(double amount);
    }

    // Low-level Implementation: Google Pay
    public static class GooglePayGateway implements PaymentGateway {
        public void processPayment(double amount) {
            System.out.println("Processing Google Pay transaction: $" + amount);
        }
    }

    // Low-level Implementation: Stripe
    public static class StripeGateway implements PaymentGateway {
        public void processPayment(double amount) {
            System.out.println("Processing Stripe card payment: $" + amount);
        }
    }

    // High-level Module (Depends ONLY on the abstraction)
    public static class CheckoutService {
        private final PaymentGateway gateway;

        // Dependency Injection via Constructor
        public CheckoutService(PaymentGateway gateway) {
            this.gateway = gateway;
        }

        public void completeCheckout(double total) {
            System.out.println("Finalizing order...");
            gateway.processPayment(total);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Step 05: Dependency Inversion Principle (GPay/Stripe) ===");
        
        // Checkout using Google Pay
        CheckoutService checkoutGPay = new CheckoutService(new GooglePayGateway());
        checkoutGPay.completeCheckout(49.99);

        System.out.println("---");

        // Checkout using Stripe (Zero changes to CheckoutService required)
        CheckoutService checkoutStripe = new CheckoutService(new StripeGateway());
        checkoutStripe.completeCheckout(49.99);
    }
}
