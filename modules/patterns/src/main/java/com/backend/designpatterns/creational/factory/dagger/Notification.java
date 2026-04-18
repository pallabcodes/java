package com.backend.designpatterns.creational.factory.dagger;

/**
 * The abstraction that our High-level services will depend on.
 */
public interface Notification {
    void send(String message);
}
