package com.backend.designpatterns.structural.adapter;

// Response DTO
public class StripeResponse {

    private final String id;
    private final double amount;
    private final String currency;
    private final String status;

    public StripeResponse(
            String id,
            double amount,
            String currency,
            String status
    ) {
        this.id = id;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }

    public String getId() { return id; }
    public double getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getStatus() { return status; }
}
