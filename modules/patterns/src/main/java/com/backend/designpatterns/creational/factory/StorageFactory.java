package com.backend.designpatterns.creational.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * PRODUCTION-GRADE FACTORY (Registry Pattern)
 *
 * Why this is L5+:
 * 1. Open/Closed Principle: Can add new types via register() without editing
 * this file.
 * 2. Decoupled: Factory doesn't need to 'know' about concrete classes
 * initially.
 * 3. Configuration Support: Handles complex per-instance configuration.
 * 4. Thread-Safe: Uses ConcurrentHashMap for the registry.
 */
public final class StorageFactory {
    // For each storage type, store a function that knows how to create it.
    // The REGISTRY holds 'Producers' (Functions), not objects.
    // Multiple threads may: register, create which is why used ConcurrentHashMap
    private static final Map<StorageType, Function<StorageConfig, Storage>> REGISTRY = new ConcurrentHashMap<>();

    /**
     * Entry point for clients.
     * 
     * [PHASE 2: EXECUTION / THE HANDOFF]
     * 1. Factory receives the 'config' (secrets/data) from the Client.
     * 2. Factory retrieves the 'trigger' (Function) from the REGISTRY.
     * 3. 'provider.apply(config)' finally EXECUTES the constructor logic.
     * 4. This is when the Object is actually born in the Heap.
     */
    public static Storage create(StorageType type, StorageConfig config) {
        Function<StorageConfig, Storage> provider = REGISTRY.get(type);

        if (provider == null) {
            throw new IllegalArgumentException("Unsupported storage type: " + type +
                    ". Use StorageFactory.register() to add support.");
        }

        // The 'Trigger' is pulled here. The Factory hands the config to the Provider.
        // so e.g. if the provider S3Storage the below does new S3Storage(config);
        return provider.apply(config);
    }

    /**
     * The 'Magic' for L5 systems: Dynamic Registration.
     * Allows separate modules (like StorageModule) to "teach" the factory new
     * types.
     */
    public static void register(StorageType type, Function<StorageConfig, Storage> provider) {
        REGISTRY.put(type, provider); // store how to create this type
    }
}
