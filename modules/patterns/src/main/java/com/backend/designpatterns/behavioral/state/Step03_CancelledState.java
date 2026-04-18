package com.backend.designpatterns.behavioral.state;

/**
 * Step 3: TERMINAL STATE
 */
public class Step03_CancelledState implements Step01_OrderState {

    @Override
    public void pay(Step02_Order order) {
        throw new IllegalStateException("Cannot pay for a cancelled order.");
    }

    @Override
    public void ship(Step02_Order order) {
        throw new IllegalStateException("Cannot ship a cancelled order.");
    }

    @Override
    public void cancel(Step02_Order order) {
        throw new IllegalStateException("Order is already cancelled.");
    }

    @Override
    public String getStatusName() {
        return "CANCELLED";
    }
}
