package com.backend.designpatterns.structural.decorator;

// Role: Concrete Component
public class EmailNotifier implements Notifier {
    @Override
    public void send(String message) {
        System.out.println("Sending Email: " + message);
    }
}
