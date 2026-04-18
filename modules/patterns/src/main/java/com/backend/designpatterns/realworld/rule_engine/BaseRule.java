package com.backend.designpatterns.realworld.rule_engine;

public abstract class BaseRule implements RuleHandler {
    protected RuleHandler next;

    @Override
    public void setNext(RuleHandler next) {
        this.next = next;
    }

    protected void passToNext(Order order) {
        if (next != null) {
            next.process(order);
        } else {
            System.out.println("Processing chain completed successfully.");
        }
    }
}
