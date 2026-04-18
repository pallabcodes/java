package com.backend.designpatterns.realworld.state_strategy;

// Context
public class Order {
    private OrderState state;
    private double weight;
    private ShippingStrategy shippingStrategy; // Current strategy

    public Order(double weight, ShippingStrategy initialShippingStrategy) {
        this.weight = weight;
        this.shippingStrategy = initialShippingStrategy;
        this.state = new NewState(); // Initial state
    }

    public void setState(OrderState state) {
        this.state = state;
    }

    public OrderState getState() {
        return state;
    }

    public double getWeight() {
        return weight;
    }

    public ShippingStrategy getShippingStrategy() {
        return shippingStrategy;
    }

    // Forwarding to State
    public void nextState() {
        state.next(this);
    }

    public void previousState() {
        state.prev(this);
    }

    public void printStatus() {
        state.printStatus();
    }
}
