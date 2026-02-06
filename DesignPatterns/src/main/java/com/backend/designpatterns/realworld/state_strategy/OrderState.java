package com.backend.designpatterns.realworld.state_strategy;

// State Interface
public interface OrderState {
    void next(Order order);
    void prev(Order order);
    void printStatus();
}
