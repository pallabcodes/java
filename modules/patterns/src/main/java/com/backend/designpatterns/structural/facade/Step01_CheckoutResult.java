package com.backend.designpatterns.structural.facade;

/**
 * Step 1: DOMAIN PAYLOADS (Simplified Output)
 */
public record Step01_CheckoutResult(
    boolean isSuccessful,
    String message,
    String trackingNumber
) {}
