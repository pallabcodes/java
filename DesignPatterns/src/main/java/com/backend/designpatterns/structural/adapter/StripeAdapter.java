package com.backend.designpatterns.structural.adapter;

// Adapter class
public class StripeAdapter implements PaymentProcessor {

    private final StripeGateway gateway;

    public StripeAdapter(StripeGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public PaymentResponse processPayment(double amount, String currency) {
        try {
            StripeResponse sdkResponse = gateway.pay(amount, currency);
            return StripeResponseMapper.map(sdkResponse);
        } catch (StripeException ex) {
            throw StripeErrorMapper.map(ex);
        }
    }
}
