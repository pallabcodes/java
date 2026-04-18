package com.backend.designpatterns.structural.adapter;

/**
 * Step 1: DOMAIN MODELS (Clean)
 */
public record Step01_PaymentRequest(
    String accountId,
    double amountInUsd,
    String idempotencyKey
) {}
