package com.backend.designpatterns.creational.factory;

// Role: Concrete Product
public class LocalStorage implements Storage {
    @Override
    public String put(String key, String value) {
        return String.format("Local Storage: Saved '%s' to /data/disk", key);
    }
}
