package com.backend.designpatterns.realworld.rule_engine;

public class AdvancedFraudStrategy implements FraudDetectionStrategy {
    @Override
    public boolean isFraudulent(Order order) {
        // Advanced: International > 5000 is fraud
        return (order.isInternational() && order.amount() > 5000) || order.amount() > 20000;
    }
}
