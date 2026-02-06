package com.backend.designpatterns.behavioral.chain_of_responsibility;

// Role: Concrete Handler
public class LoggingHandler extends Handler {

    @Override
    public void handle(Request req) {
        System.out.println("LOG: Request from " + req.getUser());
        if (next != null) next.handle(req);
    }
}
