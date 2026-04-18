package com.backend.designpatterns.creational.abstractfactory;

/**
 * Step 5: CONCRETE PRODUCT B (Local)
 */
public class Step05_ConsoleLogger implements Step02_AuditLogger {
    @Override
    public void log(String message) {
        System.out.println("Local Console: [LOG] " + message);
    }
}
