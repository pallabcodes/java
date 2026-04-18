package com.backend.designpatterns.creational.factory;

/**
 * 100% PRODUCTION-GRADE DEMO (Phase 2)
 * Demonstrates Decoupling and Lifecycle Management (Singletons vs Prototypes).
 */
public class FactoryDemo {

    public static void main(String[] args) {
        System.out.println("--- L5 Phase 2: Decoupled & Lifecycle Managed Factory ---");

        // 1. BOOTSTRAP: Teach the Factory how to create objects.
        // This 'Wiring' phase is separate from 'Usage' to keep the system decoupled.
        StorageModule.init();

        // 2. CONFIG: The Client (Demo) owns the data/secrets.
        StorageConfig config = StorageConfig.of("endpoint-alpha", "us-east");

        // 3. LIFECYCLE DEMO: S3 is registered as a Prototype.
        // Every call to 'create' pulls the '::new' trigger again, creating a NEW
        // object.
        System.out.println("\n[S3 Prototype Demo]");
        Storage s3_1 = StorageFactory.create(StorageType.S3, config);
        Storage s3_2 = StorageFactory.create(StorageType.S3, config);
        System.out.println("S3_1 Instance: " + System.identityHashCode(s3_1));
        System.out.println("S3_2 Instance: " + System.identityHashCode(s3_2));
        System.out.println("Same Instance? " + (s3_1 == s3_2)); // Should be false

        // 4. LIFECYCLE DEMO: Local is registered as a Singleton (via memoize).
        // The Factory calls the 'provider', but the provider returns the cached
        // instance.
        System.out.println("\n[Local Singleton Demo]");
        Storage local_1 = StorageFactory.create(StorageType.LOCAL, config);
        Storage local_2 = StorageFactory.create(StorageType.LOCAL, config);
        System.out.println("Local_1 Instance: " + System.identityHashCode(local_1));
        System.out.println("Local_2 Instance: " + System.identityHashCode(local_2));
        System.out.println("Same Instance? " + (local_1 == local_2)); // Should be true

        // 5. EXTENSIBILITY: Demonstrate that we can "Inject" new behavior at runtime.
        System.out.println("\n--- Testing Custom Registration ---");
        StorageFactory.register(StorageType.S3, Providers.memoize(c -> new Storage() {
            @Override
            public String name() {
                return "Custom-Mock";
            }

            @Override
            public String put(String k, String v) {
                return "Put " + k;
            }
        }));

        Storage custom1 = StorageFactory.create(StorageType.S3, config);
        Storage custom2 = StorageFactory.create(StorageType.S3, config);
        System.out.println("Custom Same Instance? " + (custom1 == custom2)); // Should be true
    }
}
