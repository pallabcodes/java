package com.backend.designpatterns.realworld.state_strategy;

public class StateStrategyDemo {

    public static void main(String[] args) {
        
        // Strategy: Express Shipping
        Order order = new Order(10.0, new ExpressShippingStrategy());
        
        order.printStatus(); // New
        
        order.nextState(); // -> Paid
        order.printStatus();
        
        order.nextState(); // -> Shipped (Calculates cost using Strategy)
        order.printStatus();
        
        order.nextState(); // -> Delivered
        order.printStatus();
    }
}
