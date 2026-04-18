package com.backend.designpatterns.structural.decorator;

/**
 * Step 4: CONCRETE DECORATOR (SMS)
 */
public class Step04_SMSDecorator extends Step03_NotifierDecorator {

    public Step04_SMSDecorator(Step01_Notifier notifier) {
        super(notifier);
    }

    @Override
    public void send(String message) {
        super.send(message);
        sendSMS(message);
    }

    private void sendSMS(String message) {
        System.out.println("Sending SMS: " + message);
    }
}
