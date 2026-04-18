package com.backend.designpatterns.structural.decorator;

/**
 * Step 4: CONCRETE DECORATOR (Slack)
 */
public class Step04_SlackDecorator extends Step03_NotifierDecorator {

    public Step04_SlackDecorator(Step01_Notifier notifier) {
        super(notifier);
    }

    @Override
    public void send(String message) {
        super.send(message);
        sendSlack(message);
    }

    private void sendSlack(String message) {
        System.out.println("Sending Slack Notification: " + message);
    }
}
