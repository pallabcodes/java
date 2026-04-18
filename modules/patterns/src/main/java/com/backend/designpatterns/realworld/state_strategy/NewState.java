package com.backend.designpatterns.realworld.state_strategy;

public class NewState implements OrderState {
    @Override
    public void next(Order order) {
        order.setState(new PaidState());
    }

    @Override
    public void prev(Order order) {
        System.out.println("The order is in its root state.");
    }

    @Override
    public void printStatus() {
        System.out.println("Order State: [New] - Waiting for payment.");
    }
}
