package com.backend.designpatterns.realworld.state_strategy;

public class PaidState implements OrderState {
    @Override
    public void next(Order order) {
        // Before moving to Shipped, we calculate the cost using the Strategy
        double cost = order.getShippingStrategy().calculateCost(order.getWeight());
        System.out.println("Payment verified. Shipping Cost calculated: $" + cost);
        
        order.setState(new ShippedState());
    }

    @Override
    public void prev(Order order) {
        order.setState(new NewState());
    }

    @Override
    public void printStatus() {
        System.out.println("Order State: [Paid] - Payment received.");
    }
}
