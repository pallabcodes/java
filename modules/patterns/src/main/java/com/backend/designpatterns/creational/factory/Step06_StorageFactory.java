package com.backend.designpatterns.creational.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Step 6: PRODUCTION-GRADE FACTORY (Registry Pattern)
 */
public final class Step06_StorageFactory {
    
    private static final Map<Step02_StorageType, Function<Step05_StorageConfig, Step01_Storage>> REGISTRY = new ConcurrentHashMap<>();

    /**
     * Entry point for clients.
     */
    public static Step01_Storage create(Step02_StorageType type, Step05_StorageConfig config) {
        Function<Step05_StorageConfig, Step01_Storage> provider = REGISTRY.get(type);

        if (provider == null) {
            throw new IllegalArgumentException("Unsupported storage type: " + type);
        }

        return provider.apply(config);
    }

    /**
     * Dynamic Registration.
     */
    public static void register(Step02_StorageType type, Function<Step05_StorageConfig, Step01_Storage> provider) {
        REGISTRY.put(type, provider);
    }
}
