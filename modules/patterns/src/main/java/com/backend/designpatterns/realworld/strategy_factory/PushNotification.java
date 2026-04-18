package com.backend.designpatterns.realworld.strategy_factory;

// Concrete Strategy C
public class PushNotification implements NotificationStrategy {
    @Override
    public void send(String message) {
        System.out.println("Sending Push Notification: " + message);
    }
}
