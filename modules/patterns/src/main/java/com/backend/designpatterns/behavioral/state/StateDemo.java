package com.backend.designpatterns.behavioral.state;

public class StateDemo {

    public static void main(String[] args) {
        System.out.println("=== L5 State Pattern Demo (Finite State Machine) ===\n");

        // Scenario 1: The Happy Path (Pending -> Paid -> Shipped)
        System.out.println("--- Scenario 1: Happy Path ---");
        Step02_Order happyOrder = new Step02_Order("ORD-1001");
        happyOrder.pay();
        happyOrder.ship();
        System.out.println("Final Status: " + happyOrder.getStatus() + "\n");

        // Scenario 2: Allowed Cancellation (Pending -> Cancelled)
        System.out.println("--- Scenario 2: Cancelled Order ---");
        Step02_Order cancelledOrder = new Step02_Order("ORD-1002");
        cancelledOrder.cancel();
        
        try {
            // Attempting to pay for a cancelled order
            cancelledOrder.pay();
        } catch (IllegalStateException e) {
            System.err.println("✅ Expected Error Caught: " + e.getMessage());
        }
        System.out.println("Final Status: " + cancelledOrder.getStatus() + "\n");

        // Scenario 3: Illegal FSM Transition (Skipping 'Paid' state)
        System.out.println("--- Scenario 3: Shielding against illegal transitions ---");
        Step02_Order illegalOrder = new Step02_Order("ORD-1003");
        
        try {
            // Attempting to ship a pending order
            illegalOrder.ship();
        } catch (IllegalStateException e) {
            System.err.println("✅ Expected Error Caught: " + e.getMessage());
        }

        System.out.println("\n[L5 ACHIEVEMENT]: Encapsulated transition logic into State objects. " +
                           "The Context (Order) is clean and illegal transitions are mathematically impossible.");
    }
}
