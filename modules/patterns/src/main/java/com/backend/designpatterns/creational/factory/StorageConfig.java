package com.backend.designpatterns.creational.factory;

import java.util.Map;

/**
 * Modern Java Record for immutable configuration.
 * In a real Google-scale system, this might be a Protobuf or a nested Config object.
 */
public record StorageConfig(
    String endpoint,
    String region,
    String bucketName,
    Map<String, String> credentials,
    boolean useEncryption
) {
    // Fluent builder-like constructor for convenience in demo
    public static StorageConfig of(String endpoint, String region) {
        return new StorageConfig(endpoint, region, "default-bucket", Map.of(), true);
    }
}
