package com.backend.designpatterns.realworld.state_strategy;

public class ExpressShippingStrategy implements ShippingStrategy {
    @Override
    public double calculateCost(double weight) {
        return weight * 2.5; // High cost
    }
}

class StandardShippingStrategy implements ShippingStrategy {
    @Override
    public double calculateCost(double weight) {
        return weight * 1.0; // Standard cost
    }
}
