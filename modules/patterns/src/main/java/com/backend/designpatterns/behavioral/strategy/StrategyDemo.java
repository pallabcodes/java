package com.backend.designpatterns.behavioral.strategy;

public class StrategyDemo {

    public static void main(String[] args) {
        System.out.println("=== L5 Strategy Pattern Demo (Functional Pricing Engine) ===\n");

        Step03_Cart cart = new Step03_Cart();
        cart.addItem(150.0);
        cart.addItem(100.0); // Subtotal: 250.0

        // 1. Standard Customer (No Discount)
        System.out.println("--- Scenario 1: Standard Customer ---");
        // cart uses noDiscount() by default
        System.out.println("Total: $" + cart.calculateTotal());


        // 2. VIP Customer (20% Off)
        System.out.println("\n--- Scenario 2: VIP Customer ---");
        // Dynamically inject the percentage strategy
        cart.setDiscountStrategy(Step02_DiscountStrategies.percentage(20));
        System.out.println("Total: $" + cart.calculateTotal());


        // 3. Coupon Code Applied ($30 Off, Flat Rate)
        System.out.println("\n--- Scenario 3: Flat Rate Coupon ---");
        // Dynamically swap the algorithm to flat rate
        cart.setDiscountStrategy(Step02_DiscountStrategies.flatRate(30));
        System.out.println("Total: $" + cart.calculateTotal());


        // 4. Black Friday Sale ($50 off if over $200)
        System.out.println("\n--- Scenario 4: Tiered Discount ---");
        cart.setDiscountStrategy(Step02_DiscountStrategies.tieredDiscount(200, 50));
        System.out.println("Total: $" + cart.calculateTotal());

        System.out.println("\n[L5 ACHIEVEMENT]: Algorithms were swapped at runtime instantly without " +
                           "creating explicit Subclasses or modifying the Cart's if/else logic.");
    }
}
