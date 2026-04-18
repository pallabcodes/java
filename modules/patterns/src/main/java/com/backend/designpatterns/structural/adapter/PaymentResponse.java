package com.backend.designpatterns.structural.adapter;

// unified response
public class PaymentResponse {

    private final String transactionId;
    private final double amount;
    private final String currency;
    private final PaymentStatus status;

    public PaymentResponse(
            String transactionId,
            double amount,
            String currency,
            PaymentStatus status
    ) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "PaymentResponse{" +
                "transactionId='" + transactionId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", status=" + status +
                '}';
    }
}
