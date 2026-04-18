package com.backend.designpatterns.realworld.rule_engine;

// Chain of Responsibility Handler
public interface RuleHandler {
    void setNext(RuleHandler next);
    void process(Order order);
}
