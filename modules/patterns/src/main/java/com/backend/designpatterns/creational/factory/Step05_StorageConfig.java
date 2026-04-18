package com.backend.designpatterns.creational.factory;

import java.util.Map;

/**
 * Step 5: PRODUCT CONFIGURATION (Immutable)
 */
public record Step05_StorageConfig(
    String endpoint,
    String region,
    String bucketName,
    Map<String, String> credentials,
    boolean useEncryption
) {
    /**
     * CONVENIENCE FACTORY METHOD
     */
    public static Step05_StorageConfig of(String endpoint, String region) {
        return new Step05_StorageConfig(
            endpoint, 
            region, 
            "default-bucket", 
            Map.of(), 
            true
        );
    }
}

// Using a record/config object instead of raw strings in the factory prevents
// "Parameter Long Lists.