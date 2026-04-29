package com.backend.designpatterns.creational.abstractfactory;

/**
 * THE ABSTRACT FACTORY DEMO
 * 
 * Demonstrates how to swap an entire "Family" of products at once.
 * 
 * Key takeaways:
 * 1. Consistency: You cannot mix an AWS Storage with a Local Logger. The factory ensures you get the right pair.
 * 2. Decoupling: The 'buildSystem' method doesn't know about S3 or CloudWatch. 
 *    It only knows about 'Storage' and 'Logger' (the abstractions).
 * 3. Scalability: Adding a 'GoogleCloudFactory' would not require any changes to the existing logic.
 */

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
