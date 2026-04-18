package com.backend.designpatterns.behavioral.state;

/**
 * Step 3: DISCRETE STATE
 */
public class Step03_PaidState implements Step01_OrderState {

    @Override
    public void pay(Step02_Order order) {
        throw new IllegalStateException("Order is already paid.");
    }

    @Override
    public void ship(Step02_Order order) {
        System.out.println("Dispatching order to courier...");
        order.setState(new Step03_ShippedState());
    }

    @Override
    public void cancel(Step02_Order order) {
        System.out.println("Initiating refund process...");
        System.out.println("Payment refunded.");
        order.setState(new Step03_CancelledState());
    }

    @Override
    public String getStatusName() {
        return "PAID";
    }
}
