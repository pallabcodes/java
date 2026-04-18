package com.backend.designpatterns.realworld.state_strategy;

// Strategy Interface
public interface ShippingStrategy {
    double calculateCost(double weight);
}
