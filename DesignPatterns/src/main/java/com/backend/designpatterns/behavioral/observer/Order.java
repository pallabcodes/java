package com.backend.designpatterns.behavioral.observer;

public class Order {
    private final String id;
    private final double amount;

    public Order(String id, double amount) {
        this.id = id;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Order{id='" + id + "', amount=" + amount + "}";
    }
}
