package com.backend.designpatterns.creational.abstractfactory;

public class AbstractFactoryDemo {

    public static void buildSystem(Step03_InfrastructureFactory factory) {
        System.out.println("\n--- Initiating System Build with " + factory.getClass().getSimpleName() + " ---");
        
        Step01_Storage storage = factory.createStorage();
        Step02_AuditLogger logger = factory.createLogger();

        logger.log("System startup initiated.");
        storage.persist("Initial system state: OK");
        logger.log("System build complete.");
    }

    public static void main(String[] args) {
        System.out.println("=== Abstract Factory Pattern Demo ===");

        // AWS System
        buildSystem(new Step06_AwsInfrastructureFactory());

        // Local System
        buildSystem(new Step06_LocalInfrastructureFactory());
        
        System.out.println("\n[L5 Rationale]: The 'buildSystem' method never changed, despite " +
                           "the entire infrastructure ecosystem being swapped.");
    }
}
