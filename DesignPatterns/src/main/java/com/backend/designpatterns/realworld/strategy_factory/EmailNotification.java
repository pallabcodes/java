package com.backend.designpatterns.realworld.strategy_factory;

// Concrete Strategy A
public class EmailNotification implements NotificationStrategy {
    @Override
    public void send(String message) {
        System.out.println("Sending Email: " + message);
    }
}
