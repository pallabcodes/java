package com.backend.designpatterns.behavioral.state;

/**
 * Step 1: THE STATE CONTRACT
 * 
 * Defines all possible transitions for an Order.
 */
public interface Step01_OrderState {
    
    void pay(Step02_Order order);
    
    void ship(Step02_Order order);
    
    void cancel(Step02_Order order);
    
    String getStatusName();
}
