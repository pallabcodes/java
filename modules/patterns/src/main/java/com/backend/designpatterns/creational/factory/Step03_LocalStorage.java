package com.backend.designpatterns.creational.factory;

/**
 * Step 3: CONCRETE PRODUCT (Local)
 */
public class Step03_LocalStorage implements Step01_Storage {
    private final Step05_StorageConfig config;

    public Step03_LocalStorage(Step05_StorageConfig config) {
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
