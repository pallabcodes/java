package com.backend.designpatterns.behavioral.state;

/**
 * Step 2: THE CONTEXT
 * 
 * An Order entity. It delegates all lifecycle business logic to the current `Step01_OrderState`.
 */
public class Step02_Order {
    
    private final String orderId;
    private Step01_OrderState currentState;

    public Step02_Order(String orderId) {
        this.orderId = orderId;
        // All new orders start as pending
        this.currentState = new Step03_PendingState();
        System.out.println("[Order Created] " + orderId + " is now " + currentState.getStatusName());
    }

    // --- State Delegation Methods ---

    public void pay() {
        currentState.pay(this);
    }

    public void ship() {
        currentState.ship(this);
    }

    public void cancel() {
        currentState.cancel(this);
    }

    // --- Package-Private Transition ---
    void setState(Step01_OrderState newState) {
        System.out.println("[Transition] " + this.orderId + " transitioned from " + 
                           currentState.getStatusName() + " -> " + newState.getStatusName());
        this.currentState = newState;
    }

    public String getStatus() {
        return currentState.getStatusName();
    }
}
