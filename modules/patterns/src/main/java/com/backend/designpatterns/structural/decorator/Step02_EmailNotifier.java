package com.backend.designpatterns.structural.decorator;

/**
 * Step 2: CONCRETE COMPONENT
 */
public class Step02_EmailNotifier implements Step01_Notifier {
    @Override
    public void send(String message) {
        System.out.println("Sending Email: " + message);
    }
}
