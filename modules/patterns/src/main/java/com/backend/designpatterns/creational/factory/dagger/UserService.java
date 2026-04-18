package com.backend.designpatterns.creational.factory.dagger;

import javax.inject.Inject;

/**
 * HIGH-LEVEL SERVICE
 * 
 * It depends only on the Notification abstraction.
 * It does NOT know about EmailNotification or SmsNotification.
 * It doesn't even know HOW to create them.
 */
public class UserService {

    private final Notification notification;

    @Inject
    public UserService(Notification notification) {
        this.notification = notification;
    }

    public void registerUser(String username) {
        System.out.println("Registering user: " + username);
        notification.send("Welcome " + username);
    }
}
