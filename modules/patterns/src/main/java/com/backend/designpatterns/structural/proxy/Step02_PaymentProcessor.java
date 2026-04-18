package com.backend.designpatterns.structural.proxy;

/**
 * Step 2: SUBJECT INTERFACE (Payment)
 */
public interface Step02_PaymentProcessor {
    boolean process(String account, double amount);
}
