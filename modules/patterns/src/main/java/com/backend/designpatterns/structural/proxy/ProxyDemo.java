package com.backend.designpatterns.structural.proxy;

public class ProxyDemo {

    public static void main(String[] args) {
        System.out.println("=== L5 Proxy Pattern Demo (Dynamic Proxies) ===\n");

        // [SCENARIO 1: User Repository]
        // Wrap a standard Database Repository with our Infrastructure Proxy.
        UserRepository realRepo = new DatabaseUserRepository();
        UserRepository proxyRepo = InfrastructureProxy.wrap(realRepo, UserRepository.class);

        System.out.println("--- Scenario 1: User Repository ---");
        User user = proxyRepo.findById("U001");
        System.out.println("Result: " + user.getName());


        // [SCENARIO 2: Payment Processor]
        // Wrap a completely different interface using the EXACT SAME Proxy logic.
        PaymentProcessor realStripe = new StripePaymentProcessor();
        PaymentProcessor proxyStripe = InfrastructureProxy.wrap(realStripe, PaymentProcessor.class);

        System.out.println("\n--- Scenario 2: Payment Processor ---");
        boolean success = proxyStripe.process("ACT-999", 250.00);
        System.out.println("Result: Payment Success = " + success);


        // [SCENARIO 3: Error Handling]
        System.out.println("\n--- Scenario 3: Error Shielding ---");
        try {
            proxyRepo.findById(null); // DatabaseUserRepository throws exception on null
        } catch (Exception e) {
            System.out.println("Main Application: Caught handled exception.");
        }

        System.out.println("\n[L5 ACHIEVEMENT]: Single Proxy Handler applied cross-cutting " +
                           "concerns to multiple unrelated domains.");
    }
}
