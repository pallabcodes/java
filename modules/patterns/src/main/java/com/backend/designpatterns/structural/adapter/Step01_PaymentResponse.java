package com.backend.designpatterns.structural.adapter;

/**
 * Step 1: DOMAIN MODELS (Response)
 */
public record Step01_PaymentResponse(
    String transactionId,
    boolean success,
    String failureReason
) {}
