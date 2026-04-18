package com.backend.designpatterns.realworld.rule_engine;

public class SimpleFraudStrategy implements FraudDetectionStrategy {
    @Override
    public boolean isFraudulent(Order order) {
        // Simple rule: any amount > 10000 is suspicious
        return order.amount() > 10000;
    }
}
