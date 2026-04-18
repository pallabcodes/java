package com.backend.designpatterns.structural.adapter;

// Third party SDK
public class PayPalGateway {
    public PayPalResponse makePayment(double amount, String currency) {
        return new PayPalResponse("txn_456", amount, currency, "approved");
    }
}
