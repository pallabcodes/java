package com.backend.designpatterns.behavioral.chain_of_responsibility;

// Role: Concrete Handler
public class BusinessHandler extends Handler {

    @Override
    public void handle(Request req) {
        System.out.println("PROCESSING: " + req.getPayload());
    }
}
