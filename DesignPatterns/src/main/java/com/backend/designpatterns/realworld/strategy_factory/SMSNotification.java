package com.backend.designpatterns.realworld.strategy_factory;

// Concrete Strategy B
public class SMSNotification implements NotificationStrategy {
    @Override
    public void send(String message) {
        System.out.println("Sending SMS: " + message);
    }
}
