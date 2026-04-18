package com.backend.designpatterns.creational.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * PRODUCTION-GRADE FACTORY (Registry Pattern)
 *
 * Why this is L5+:
 * 1. Open/Closed Principle: Can add new types via register() without editing this file.
 * 2. Decoupled: Factory doesn't need to 'know' about concrete classes initially.
 * 3. Configuration Support: Handles complex per-instance configuration.
 * 4. Thread-Safe: Uses ConcurrentHashMap for the registry.
 */
public final class StorageFactory {

    private static final Map<StorageType, Function<StorageConfig, Storage>> REGISTRY = new ConcurrentHashMap<>();

    /**
     * Entry point for clients.
     */
    public static Storage create(StorageType type, StorageConfig config) {
        Function<StorageConfig, Storage> provider = REGISTRY.get(type);
        
        if (provider == null) {
            throw new IllegalArgumentException("Unsupported storage type: " + type + 
                ". Use StorageFactory.register() to add support.");
        }
        
        return provider.apply(config);
    }

    /**
     * The 'Magic' for L5 systems: Dynamic Registration.
     * Allows juniors or other teams to plug in new implementations.
     */
    public static void register(StorageType type, Function<StorageConfig, Storage> provider) {
        REGISTRY.put(type, provider);
    }
}
