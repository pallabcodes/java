package com.backend.designpatterns.creational.factory;

// Role: Concrete Product
public class S3Storage implements Storage {
    private final StorageConfig config;

    public S3Storage(StorageConfig config) {
        this.config = config;
    }

    @Override
    public String name() {
        return "S3 [" + config.region() + "]";
    }

    @Override
    public String put(String key, String value) {
        return String.format("S3 Storage (%s): Uploaded '%s' to bucket '%s'", 
            config.region(), key, config.bucketName());
    }
}
