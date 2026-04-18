package com.backend.designpatterns.structural.adapter;

// Subsystem for Facade
public class TransactionLogger {

    public void log(String message) {
        System.out.println("[LOG] " + message);
    }
}
