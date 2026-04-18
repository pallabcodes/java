package com.backend.designpatterns.creational.factory;

// Role: Product Interface
public interface Storage {
    String name();
    String put(String key, String value);
}
