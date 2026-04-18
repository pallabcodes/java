package com.backend.designpatterns.structural.facade;

public class FacadeDemo {

    public static void main(String[] args) {
        System.out.println("=== L5 Facade Pattern Demo (Application Orchestration) ===\n");

        // The client only needs the Facade
        Step03_OrderFulfillmentFacade checkoutFacade = new Step03_OrderFulfillmentFacade();

        // SCENARIO A: A Successful Checkout
        System.out.println("--- Scenario A: Valid Payment ---");
        Step01_CheckoutRequest validRequest = new Step01_CheckoutRequest(
            "MACBOOK-PRO-M4", 
            1, 
            2499.99, 
            "4111-2222-3333-4444", 
            "1600 Amphitheatre Parkway, Mountain View, CA", 
            "customer@google.com"
        );
        
        Step01_CheckoutResult validResult = checkoutFacade.checkout(validRequest);
        System.out.println("Client Application Received: " + validResult + "\n");


        // SCENARIO B: A Failed Checkout
        System.out.println("--- Scenario B: Declined Payment ---");
        Step01_CheckoutRequest invalidRequest = new Step01_CheckoutRequest(
            "AIRPODS-PRO", 
            2, 
            499.98, 
            "4111-2222-3333-0000", 
            "Infinite Loop, Cupertino, CA", 
            "broke@example.com"
        );
        
        Step01_CheckoutResult invalidResult = checkoutFacade.checkout(invalidRequest);
        System.out.println("Client Application Received: " + invalidResult + "\n");
    }
}
