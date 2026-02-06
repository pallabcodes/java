package com.backend.designpatterns.structural.adapter;

// Adapter class
public class PayPalAdapter implements PaymentProcessor {

    private final PayPalGateway gateway;

    public PayPalAdapter(PayPalGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public PaymentResponse processPayment(double amount, String currency) {
        try {
            PayPalResponse sdkResponse = gateway.makePayment(amount, currency);
            return PayPalResponseMapper.map(sdkResponse);
        } catch (PayPalException ex) {
            throw PayPalErrorMapper.map(ex);
        }
    }
}
