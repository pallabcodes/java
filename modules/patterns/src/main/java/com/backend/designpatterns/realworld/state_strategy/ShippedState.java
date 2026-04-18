package com.backend.designpatterns.realworld.state_strategy;

public class ShippedState implements OrderState {
    @Override
    public void next(Order order) {
        System.out.println("Order delivered.");
        order.setState(new DeliveredState());
    }

    @Override
    public void prev(Order order) {
        order.setState(new PaidState());
    }

    @Override
    public void printStatus() {
        System.out.println("Order State: [Shipped] - On the way.");
    }
}
