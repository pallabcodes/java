package com.backend.designpatterns.realworld.rule_engine;

// Strategy Interface
public interface FraudDetectionStrategy {
    boolean isFraudulent(Order order);
}
