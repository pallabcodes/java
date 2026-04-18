package com.backend.designpatterns.creational.factory;

/**
 * BOOTSTRAP LAYER (Dependency Injection Module)
 *
 * This class handles the manual "wiring" of the system.
 * By moving registration here, StorageFactory is now 100% decoupled from concrete implementations.
 */
public final class StorageModule {

    /**
     * Bootstraps the application.
     */
    public static void init() {
        // [PHASE 1: REGISTRATION]
        // S3Storage::new is a Method Reference acting as a "Lazy Trigger".
        // It's a Function<StorageConfig, Storage> that is STORED, but NOT EXECUTED yet.
        // No S3Storage instance is created in memory at this point.
        StorageFactory.register(StorageType.S3, S3Storage::new);

        // [LIFECYCLE MANAGEMENT]
        // Here we "wrap" the constructor in a memoizer.
        // The Factory is 100% unaware that this will return a Singleton.
        // This moves Lifecycle policy out of the classes and into this Module layer.
        StorageFactory.register(StorageType.LOCAL, Providers.memoize(LocalStorage::new));
    }
}
