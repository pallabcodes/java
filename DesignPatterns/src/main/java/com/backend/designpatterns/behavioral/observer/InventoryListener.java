package com.backend.designpatterns.behavioral.observer;

// Role: Concrete Observer
public class InventoryListener implements OrderListener {
    @Override
    public void onOrderCreated(Order order) {
        System.out.println("INVENTORY: Reserving stock for " + order);
    }
}
