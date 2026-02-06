package com.backend.designpatterns.behavioral.observer;

// Role: Concrete Observer
public class EmailListener implements OrderListener {
    @Override
    public void onOrderCreated(Order order) {
        System.out.println("EMAIL: Sending confirmation for " + order);
    }
}
