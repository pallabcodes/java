package com.backend.designpatterns.behavioral.state;

/**
 * Step 3: TERMINAL STATE
 */
public class Step03_ShippedState implements Step01_OrderState {

    @Override
    public void pay(Step02_Order order) {
        throw new IllegalStateException("Cannot pay for an order that is already shipped.");
    }

    @Override
    public void ship(Step02_Order order) {
        throw new IllegalStateException("Order is already shipped.");
    }

    @Override
    public void cancel(Step02_Order order) {
        throw new IllegalStateException("Cannot cancel an order that has already been shipped.");
    }

    @Override
    public String getStatusName() {
        return "SHIPPED";
    }
}
