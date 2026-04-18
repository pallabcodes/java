package com.backend.designpatterns.behavioral.chain_of_responsibility;

// Role: Abstract Handler
public abstract class Handler {

    protected Handler next;

    public Handler setNext(Handler next) {
        this.next = next;
        return next; // returning next allows chaining .setNext().setNext()
    }

    public abstract void handle(Request req);
}
