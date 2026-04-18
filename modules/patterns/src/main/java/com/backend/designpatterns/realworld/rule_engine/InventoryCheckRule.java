package com.backend.designpatterns.realworld.rule_engine;

public class InventoryCheckRule extends BaseRule {
    @Override
    public void process(Order order) {
        System.out.println("Checking Inventory...");
        // Simulate inventory check logic
        passToNext(order);
    }
}
