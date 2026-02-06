package com.backend.designpatterns.realworld.chain_builder;

// Role: Abstract Handler
public abstract class ValidationHandler {
    protected ValidationHandler next;

    public ValidationHandler setNext(ValidationHandler next) {
        this.next = next;
        return next;
    }

    public abstract void validate(UserContext ctx);
}
