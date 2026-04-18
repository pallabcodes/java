package com.backend.designpatterns.realworld.strategy_factory;

// Factory Class to encapsulate object creation
public class NotificationFactory {

    public static NotificationStrategy getStrategy(String channel) {
        return switch (channel.toLowerCase()) {
            case "email" -> new EmailNotification();
            case "sms" -> new SMSNotification();
            case "push" -> new PushNotification();
            default -> throw new IllegalArgumentException("Unknown channel: " + channel);
        };
    }
}
