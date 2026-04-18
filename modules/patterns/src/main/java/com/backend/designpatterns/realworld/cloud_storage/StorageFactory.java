package com.backend.designpatterns.realworld.cloud_storage;

// Factory Pattern
public class StorageFactory {

    public static CloudStorage getStorage(String provider) {
        return switch (provider.toLowerCase()) {
            case "aws" -> new S3Adapter();
            case "gcp" -> new GoogleCloudAdapter();
            default -> throw new IllegalArgumentException("Unknown provider: " + provider);
        };
    }
}
