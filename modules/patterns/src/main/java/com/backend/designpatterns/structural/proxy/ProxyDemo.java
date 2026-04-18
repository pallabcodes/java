package com.backend.designpatterns.structural.proxy;

public class ProxyDemo {

    public static void main(String[] args) {
        System.out.println("=== L5 Proxy Pattern Demo (Dynamic Proxies) ===\n");

        // Scenario 1: User Repository
        Step02_UserRepository realRepo = new Step03_DatabaseUserRepository();
        Step02_UserRepository proxyRepo = Step04_InfrastructureProxy.wrap(realRepo, Step02_UserRepository.class);

        System.out.println("--- Scenario 1: User Repository ---");
        Step01_User user = proxyRepo.findById("U001");
        System.out.println("Result: " + user.getName());


        // Scenario 2: Payment Processor
        Step02_PaymentProcessor realStripe = new Step03_StripePaymentProcessor();
        Step02_PaymentProcessor proxyStripe = Step04_InfrastructureProxy.wrap(realStripe, Step02_PaymentProcessor.class);

        System.out.println("\n--- Scenario 2: Payment Processor ---");
        boolean success = proxyStripe.process("ACT-999", 250.00);
        System.out.println("Result: Payment Success = " + success);


        // Scenario 3: Error Handling
        System.out.println("\n--- Scenario 3: Error Shielding ---");
        try {
            proxyRepo.findById(null);
        } catch (Exception e) {
            System.out.println("Main Application: Caught handled exception.");
        }
    }
}
