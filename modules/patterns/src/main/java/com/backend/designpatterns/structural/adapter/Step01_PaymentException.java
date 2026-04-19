package com.backend.designpatterns.structural.adapter;

/**
 * Step 1: DOMAIN MODELS (Exceptions)
 * 
 * L7 Mastery: We use custom Unchecked Exceptions (RuntimeException) to keep APIs clean.
 * 
 * Why Throwable cause? (Abstraction vs. Observability)
 * 1. DECOUPLING: If low-level code throws a SocketTimeoutException, wrapping it in 
 *    PaymentException prevents the higher-level service from being coupled to a specific 
 *    network library or failure mode. The business logic only cares that "Payment Failed".
 * 
 * 2. EVIDENCE: In a distributed system, this is "Crime Scene Investigation". By 
 *    preserving the 'cause' (root exception), logs will show both the high-level 
 *    business event and the underlying "smoking gun" (e.g., exact port/IP timeout), 
 *    enabling SREs to debug production issues without losing context.
 */
public class Step01_PaymentException extends RuntimeException {
    public Step01_PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
