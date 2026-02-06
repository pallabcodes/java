package com.backend.designpatterns.realworld.rule_engine;

public class FraudCheckRule extends BaseRule {
    private final FraudDetectionStrategy strategy;

    public FraudCheckRule(FraudDetectionStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void process(Order order) {
        System.out.println("Checking Fraud Rules...");
        if (strategy.isFraudulent(order)) {
            throw new RuntimeException("Fraud detected! Order rejected.");
        }
        passToNext(order);
    }
}
