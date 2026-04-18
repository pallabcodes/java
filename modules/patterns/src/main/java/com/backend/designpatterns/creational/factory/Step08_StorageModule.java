package com.backend.designpatterns.creational.factory;

/**
 * Step 8: BOOTSTRAP LAYER (WIRING)
 */
public final class Step08_StorageModule {

    public static void init() {
        // Prototype Registration
        Step06_StorageFactory.register(Step02_StorageType.S3, Step04_S3Storage::new);

        // Singleton Registration (Memoized)
        Step06_StorageFactory.register(Step02_StorageType.LOCAL, Step07_Providers.memoize(Step03_LocalStorage::new));
    }
}
