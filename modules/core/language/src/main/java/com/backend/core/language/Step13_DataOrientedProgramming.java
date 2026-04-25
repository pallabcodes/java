package com.backend.core.language;

/**
 * Step 13: Data-Oriented Programming (DOP)
 * 
 * L7 Mastery:
 * 1. Logic via Pattern Matching: Moving logic out of classes and into 'switch' expressions.
 * 2. Exhaustiveness: Sealed hierarchies ensure the compiler catches unhandled types.
 * 3. Records as Data: Pure, immutable data carriers without behavior bloat.
 */
public class Step13_DataOrientedProgramming {

    // 1. Data Modeling (Sealed Hierarchy)
    sealed interface Event permits Login, Logout, Transaction {}
    
    record Login(String user, long timestamp) implements Event {}
    record Logout(String user) implements Event {}
    record Transaction(String user, double amount, String currency) implements Event {}

    // 2. Pure Logic (Pattern Matching)
    public static String handleEvent(Event event) {
        return switch (event) {
            case Login(var user, var time) -> 
                String.format("User %s logged in at %d", user, time);
            
            case Logout(var user) -> 
                String.format("User %s logged out", user);
            
            case Transaction(var user, var amount, var currency) when amount > 1000 -> 
                String.format("⚠️ LARGE Transaction: %s moved %.2f %s", user, amount, currency);
            
            case Transaction(var user, var amount, var currency) -> 
                String.format("Transaction: %s moved %.2f %s", user, amount, currency);
            
            // L7 Note: No 'default' needed! Compiler knows Event is sealed.
        };
    }

    public static void main(String[] args) {
        System.out.println("=== Step 13: Data-Oriented Programming (Java 21 Holy Trinity) ===");
        
        Event[] events = {
            new Login("alice_l7", System.currentTimeMillis()),
            new Transaction("bob_l5", 50.0, "USD"),
            new Transaction("charlie_l9", 5000.0, "BTC"),
            new Logout("alice_l7")
        };

        for (Event e : events) {
            System.out.println("Processing: " + handleEvent(e));
        }

        System.out.println("\nL5 Insight: This approach separates 'Data' (Records) from 'Logic' (Switch).");
        System.out.println("L7 Depth: This is the 'Algebraic Data Types' approach in Java, replacing the Visitor Pattern.");
    }
}
