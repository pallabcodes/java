package com.backend.designpatterns.structural.facade;

/**
 * Step 1: DOMAIN PAYLOADS (Simplified Input)
 */
public record Step01_CheckoutRequest(
    String productId,
    int quantity,
    double totalAmount,
    String creditCardNumber,
    String shippingAddress,
    String email
) {}
