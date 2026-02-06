package com.backend.designpatterns.behavioral.observer;

import java.util.ArrayList;
import java.util.List;

// Role: Subject (Publisher)
public class OrderPublisher {

    private final List<OrderListener> listeners = new ArrayList<>();

    public void subscribe(OrderListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(OrderListener listener) {
        listeners.remove(listener);
    }

    public void publishOrderCreated(Order order) {
        System.out.println("PUBLISHING: New Order " + order);
        for (OrderListener listener : listeners) {
            listener.onOrderCreated(order);
        }
    }
}
