package com.backend.designpatterns.creational.factory;

public class FactoryDemo {

    public static void main(String[] args) {
        System.out.println("--- L5 Phase: Sequential Factory Refactor ---");

        // 1. BOOTSTRAP
        Step08_StorageModule.init();

        // 2. CONFIG
        Step05_StorageConfig config = Step05_StorageConfig.of("endpoint-alpha", "us-east");

        // 3. S3 Prototype
        System.out.println("\n[S3 Prototype Demo]");
        Step01_Storage s3_1 = Step06_StorageFactory.create(Step02_StorageType.S3, config);
        Step01_Storage s3_2 = Step06_StorageFactory.create(Step02_StorageType.S3, config);
        System.out.println("Same Instance? " + (s3_1 == s3_2));

        // 4. Local Singleton
        System.out.println("\n[Local Singleton Demo]");
        Step01_Storage local_1 = Step06_StorageFactory.create(Step02_StorageType.LOCAL, config);
        Step01_Storage local_2 = Step06_StorageFactory.create(Step02_StorageType.LOCAL, config);
        System.out.println("Same Instance? " + (local_1 == local_2));

        // 5. EXTENSIBILITY
        System.out.println("\n--- Testing Custom Registration ---");
        Step06_StorageFactory.register(Step02_StorageType.S3, Step07_Providers.memoize(c -> new Step01_Storage() {
            @Override
            public String name() { return "Custom-Mock"; }
            @Override
            public String put(String k, String v) { return "Put " + k; }
        }));

        Step01_Storage custom1 = Step06_StorageFactory.create(Step02_StorageType.S3, config);
        Step01_Storage custom2 = Step06_StorageFactory.create(Step02_StorageType.S3, config);
        System.out.println("Custom Same Instance? " + (custom1 == custom2));
    }
}
