package com.backend.designpatterns.creational.factory;

import java.util.Map;

/**
 * Modern Java Record for immutable configuration.
 * 
 * WHY USE A RECORD? (Google L5 Principle)
 * 1. IMMUTABILITY: Records are final and fields are final. Once created, a config cannot be 
 *    mutated, preventing "Race Conditions" in high-concurrency environments.
 * 2. CORRECTNESS: Eliminates boilerplate (getters, hashCode, equals) that is often 
 *    the source of subtle bugs.
 * 3. THREAD-SAFETY: Safe to share across multiple threads without synchronization.
 */
public record StorageConfig(
    String endpoint,
    String region,
    String bucketName,
    Map<String, String> credentials,
    boolean useEncryption
) {
    /**
     * CONVENIENCE FACTORY METHOD (Static Factory Pattern)
     * 
     * This provides "Fluent Defaults" and encapsulates configuration complexity.
     * 
     * WHY Map.of()?
     * - This is conceptually {} (an empty JSON object), NOT null. 
     * - Returns an UNMODIFIABLE map which throws an error if anyone tries to change it.
     * - Prevents NullPointerExceptions while enforcing strict "Correctness by Design."
     */
    public static StorageConfig of(String endpoint, String region) {
        return new StorageConfig(
            endpoint, 
            region, 
            "default-bucket", 
            Map.of(), // Shape: {} - Safe, immutable alternative to null
            true
        );
    }
}

// Using a record/config object instead of raw strings in the factory prevents
// "Parameter Long Lists.