package com.backend.designpatterns.creational.factory;

// Role: Product Interface (and its role : Abstract Product)
// In a professional system, the client only interacts with this interface. It defines what a storage service can do (its behavior) without caring how it's done.

// N.B: Looking at this we could use an abstract class to do same i.e. only implement what this Storage can do (behaviours) not how?
public interface Storage {
    String name();
    String put(String key, String value);
}
