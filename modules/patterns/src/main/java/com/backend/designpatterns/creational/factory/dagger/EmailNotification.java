package com.backend.designpatterns.creational.factory.dagger;

import javax.inject.Inject;

/**
 * Concrete implementation.
 */
public class EmailNotification implements Notification {

    @Inject
    public EmailNotification() {
        // Constructor injection allowed even for concrete types
    }

    @Override
    public void send(String message) {
        System.out.println("Email: " + message);
    }
}
