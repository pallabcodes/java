package com.backend.designpatterns.structural.decorator;

// Role: Concrete Decorator
public class SMSDecorator extends NotifierDecorator {

    public SMSDecorator(Notifier notifier) {
        super(notifier);
    }

    @Override
    public void send(String message) {
        super.send(message); // Delegate to wrapped object
        sendSMS(message);    // Add extra behavior
    }

    private void sendSMS(String message) {
        System.out.println("Sending SMS: " + message);
    }
}
