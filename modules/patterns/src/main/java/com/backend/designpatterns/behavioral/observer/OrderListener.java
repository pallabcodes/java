package com.backend.designpatterns.behavioral.observer;

// Role: Observer Interface
public interface OrderListener {
    void onOrderCreated(Order order);
}
