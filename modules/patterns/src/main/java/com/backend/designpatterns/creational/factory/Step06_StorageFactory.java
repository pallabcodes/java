package com.backend.designpatterns.creational.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Step 6: PRODUCTION-GRADE FACTORY (Registry Pattern)
 * 
 * This is a "Dispatcher" style factory. Instead of hardcoding if-else blocks,
 * it uses a Registry (a Map) to store creation logic.
 * 
 * DESIGN CONCEPTS:
 * 1. Open/Closed Principle: Add new storage types without editing this class.
 * 2. Decoupling: The factory doesn't know about concrete classes like DiskStorage.
 * 3. Thread-Safe: Uses ConcurrentHashMap for safe parallel access.
 */
public final class Step06_StorageFactory {
    
    /**
     * THE REGISTRY (The Brain)
     * Maps a StorageType to a "Provider" function.
     * Provider = (Config) -> new ConcreteStorage(Config)
     */
    private static final Map<Step02_StorageType, Function<Step05_StorageConfig, Step01_Storage>> REGISTRY = new ConcurrentHashMap<>();

    /**
     * THE ENTRY POINT
     * 1. Find the "recipe" (provider) for the requested type.
     * 2. Fail fast with an error if no recipe exists.
     * 3. Call the recipe with the config to create the object.
     */
    public static Step01_Storage create(Step02_StorageType type, Step05_StorageConfig config) {
        Function<Step05_StorageConfig, Step01_Storage> provider = REGISTRY.get(type);

        if (provider == null) {
            throw new IllegalArgumentException("Unsupported storage type: " + type);
        }

        return provider.apply(config);
    }

    /**
     * THE PLUG-IN POINT (Dynamic Registration)
     * Allows new implementations to be added at runtime without changing the Factory code.
     */
    public static void register(Step02_StorageType type, Function<Step05_StorageConfig, Step01_Storage> provider) {
        REGISTRY.put(type, provider);
    }
}
