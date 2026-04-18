package com.backend.designpatterns.structural.proxy;

/**
 * A second interface to prove that InfrastructureProxy is GENERIC.
 */
public interface PaymentProcessor {
    boolean process(String account, double amount);
}
