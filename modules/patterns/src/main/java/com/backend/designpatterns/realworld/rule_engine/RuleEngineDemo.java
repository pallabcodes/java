package com.backend.designpatterns.realworld.rule_engine;

public class RuleEngineDemo {

    public static void main(String[] args) {
        
        // 1. Build the Chain
        // We inject the Strategy (AdvancedFraudStrategy) into the Chain Handler (FraudCheckRule)
        FraudCheckRule fraudRule = new FraudCheckRule(new AdvancedFraudStrategy());
        InventoryCheckRule inventoryRule = new InventoryCheckRule();
        
        fraudRule.setNext(inventoryRule); // Chain: Fraud -> Inventory

        // 2. Process Orders
        Order order1 = new Order("ORD_001", 6000, true); // International > 5k (Fraud!)
        
        try {
            System.out.println("Processing Order 1...");
            fraudRule.process(order1);
        } catch (Exception e) {
            System.out.println("Result: " + e.getMessage());
        }

        Order order2 = new Order("ORD_002", 1000, false); // Safe
        System.out.println("\nProcessing Order 2...");
        fraudRule.process(order2);
    }
}
