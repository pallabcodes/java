package com.backend.designpatterns.creational.abstractfactory;

/**
 * ABSTRACT FACTORY DEMO
 * 
 * Notice that this client code is 100% decoupled from specific implementations.
 * It only knows about 'InfrastructureFactory', 'Storage', and 'AuditLogger'.
 */
public class AbstractFactoryDemo {

    /**
     * This method acts as our Application Orchestrator.
     * It builds a system using WHATEVER factory it is given.
     */
    public static void buildSystem(InfrastructureFactory factory) {
        System.out.println("\n--- Initiating System Build with " + factory.getClass().getSimpleName() + " ---");
        
        // The factory ensures these two are from the SAME family.
        Storage storage = factory.createStorage();
        AuditLogger logger = factory.createLogger();

        // Use the infrastructure
        logger.log("System startup initiated.");
        storage.persist("Initial system state: OK");
        logger.log("System build complete.");
    }

    public static void main(String[] args) {
        System.out.println("=== Abstract Factory Pattern Demo ===");

        // [SCENARIO 1]: Deploying to AWS
        // We simply pass the AWS factory.
        buildSystem(new AwsInfrastructureFactory());

        // [SCENARIO 2]: Deploying to Local Staging / Development
        // We simply swap for the Local factory.
        buildSystem(new LocalInfrastructureFactory());
        
        System.out.println("\n[L5 Rationale]: The 'buildSystem' method never changed, despite " +
                           "the entire infrastructure ecosystem being swapped.");
    }
}
