package com.backend.designpatterns.structural.adapter;

// Demo class
public class AdapterPatternDemo {

    public static void main(String[] args) {

        PaymentService paymentService = new PaymentService();

        // Pay using Stripe
        PaymentResponse r1 = paymentService.pay("stripe", 1000, "USD");
        System.out.println("Stripe: " + r1);

        // Pay using PayPal
        PaymentResponse r2 = paymentService.pay("paypal", 2000, "USD");
        System.out.println("PayPal: " + r2);
    }
}
