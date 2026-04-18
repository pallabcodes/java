package com.backend.designpatterns.realworld.state_strategy;

public class DeliveredState implements OrderState {
    @Override
    public void next(Order order) {
         System.out.println("Order is already delivered.");
    }

    @Override
    public void prev(Order order) {
        order.setState(new ShippedState());
    }

    @Override
    public void printStatus() {
        System.out.println("Order State: [Delivered] - Review requested.");
    }
}
