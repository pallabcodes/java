package com.backend.designpatterns.behavioral.state;

/**
 * Step 3: DISCRETE STATE
 */
public class Step03_PendingState implements Step01_OrderState {

    @Override
    public void pay(Step02_Order order) {
        System.out.println("Processing payment...");
        order.setState(new Step03_PaidState());
    }

    @Override
    public void ship(Step02_Order order) {
        throw new IllegalStateException("Cannot ship an unpaid order.");
    }

    @Override
    public void cancel(Step02_Order order) {
        System.out.println("Cancelling pending order.");
        order.setState(new Step03_CancelledState());
    }

    @Override
    public String getStatusName() {
        return "PENDING";
    }
}
