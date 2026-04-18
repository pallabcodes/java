package com.backend.designpatterns.structural.decorator;

public class DecoratorDemo {

    public static void main(String[] args) {
        System.out.println("--- Decorator Pattern Demo ---");

        // Use Case: Use Decorator to add behavior to objects dynamically at runtime without 
        // affecting other objects of the same class (e.g., Adding SMS/Slack to Email Notifier).

        // Stack: Email + SMS + Slack
        Notifier notifier = new SlackDecorator(
                                new SMSDecorator(
                                    new EmailNotifier()
                                )
                            );
        
        notifier.send("Server is down! Urgent attention required.");
    }
}
