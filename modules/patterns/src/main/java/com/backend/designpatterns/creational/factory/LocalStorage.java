package com.backend.designpatterns.creational.factory;

// Role: Concrete Product
public class LocalStorage implements Storage {
    private final StorageConfig config;

    public LocalStorage(StorageConfig config) {
        this.config = config;
    }

    @Override
    public String name() {
        return "LocalFileSystem [" + config.endpoint() + "]";
    }

    @Override
    public String put(String key, String value) {
        return String.format("Local Storage: Saved '%s' to %s", key, config.endpoint());
    }
}
