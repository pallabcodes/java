package com.backend.designpatterns.creational.factory.dagger;

import dagger.Component;
import javax.inject.Singleton;

/**
 * THE REPOSITORY (Component)
 * 
 * This is the bridge. Dagger will generate a class named "DaggerNotificationComponent"
 * which implements this interface. 
 * 
 * This is the "explicit" part of Google's DI: you define exactly what services 
 * are available to be requested.
 */
@Component(modules = NotificationModule.class)
public interface NotificationComponent {
    
    // This allows us to request a fully-formed UserService from the component
    UserService getUserService();
}
