package com.backend.designpatterns.realworld.strategy_factory;

// Demo / Client
public class NotificationDemo {

    public static void main(String[] args) {
        // Client only knows about the Factory and the Strategy Interface
        // It doesn't need to know concrete classes (EmailNotification, etc.)
        
        String channel = "email"; // Could come from config or user input
        NotificationStrategy strategy = NotificationFactory.getStrategy(channel);
        strategy.send("Hello via Email!");

        channel = "push";
        strategy = NotificationFactory.getStrategy(channel);
        strategy.send("Hello via Push!");
    }
}
