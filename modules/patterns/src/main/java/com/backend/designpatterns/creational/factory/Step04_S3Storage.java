package com.backend.designpatterns.creational.factory;

/**
 * Step 4: CONCRETE PRODUCT (Cloud)
 * 
 * This is another implementation of the Storage interface.
 * It contains logic for interacting with AWS S3.
 */
public class Step04_S3Storage implements Step01_Storage {
    private final Step05_StorageConfig config;

    public Step04_S3Storage(Step05_StorageConfig config) {
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
