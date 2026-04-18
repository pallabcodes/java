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
     * In a real Google system, this would be handled by Guice, Dagger, or a Framework.
     */
    public static void init() {
        // Register S3 as Prototype (New instance every time)
        StorageFactory.register(StorageType.S3, S3Storage::new);

        // Register LOCAL as Singleton (Same instance every time)
        // This is a powerful L5 pattern: The factory doesn't know it's a singleton!
        StorageFactory.register(StorageType.LOCAL, Providers.memoize(LocalStorage::new));
    }
}
