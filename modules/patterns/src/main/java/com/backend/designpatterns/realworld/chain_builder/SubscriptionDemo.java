package com.backend.designpatterns.realworld.chain_builder;

public class SubscriptionDemo {

    public static void main(String[] args) {
        System.out.println("--- Chain + Builder Demo (Netflix Subscription Flow) ---");

        // 1. Build the Validation Chain
        ValidationHandler pipeline = new FraudCheckHandler();
        pipeline.setNext(new RegionCheckHandler());

        // 2. Scenario A: Valid User
        System.out.println("\n[Scenario A] Processing Valid User...");
        UserContext validUser = new UserContext.Builder("U-100")
                .region("US")
                .mobile(true)
                .build();
        pipeline.validate(validUser);

        // 3. Scenario B: Fraudulent User
        System.out.println("\n[Scenario B] Processing Fraudulent User...");
        UserContext fraudUser = new UserContext.Builder("U-666")
                .flagged(true)
                .build();
        try {
            pipeline.validate(fraudUser);
        } catch (RuntimeException e) {
            System.out.println("Result: " + e.getMessage());
        }
    }
}
