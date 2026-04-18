package com.backend.designpatterns.creational.factory.dagger;

import dagger.Module;
import dagger.Provides;

/**
 * GOOGLE-STYLE MODULE
 * 
 * This is where we "teach" Dagger how to provide interfaces.
 * Because you can't @Inject into an interface.
 */
@Module
public class NotificationModule {

    @Provides
    public Notification provideNotification(EmailNotification emailNotification) {
        // Here we decide that EmailNotification is the default implementation for Notification
        return emailNotification;
    }
}
