package com.backend.designpatterns.structural.adapter;

// Response DTO
public class PayPalResponse {

    private final String transactionId;
    private final double value;
    private final String currencyCode;
    private final String state;

    public PayPalResponse(
            String transactionId,
            double value,
            String currencyCode,
            String state
    ) {
        this.transactionId = transactionId;
        this.value = value;
        this.currencyCode = currencyCode;
        this.state = state;
    }

    public String getTransactionId() { return transactionId; }
    public double getValue() { return value; }
    public String getCurrencyCode() { return currencyCode; }
    public String getState() { return state; }
}
