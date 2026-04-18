package com.backend.designpatterns.behavioral.chain_of_responsibility;

// Role: Concrete Handler
public class AuthHandler extends Handler {

    @Override
    public void handle(Request req) {
        if (!req.isAuthenticated()) {
            System.out.println("AUTH FAILED: Request denied.");
            return; // Break chain
        }
        System.out.println("AUTH SUCCESS");
        if (next != null) next.handle(req);
    }
}
