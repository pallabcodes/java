package com.backend.designpatterns.creational.abstractfactory;

/**
 * Concrete Product B2 (Local Family)
 */
public class ConsoleLogger implements AuditLogger {
    @Override
    public void log(String message) {
        System.out.println("Local Console: [LOG] " + message);
    }
}
