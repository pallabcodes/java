package com.backend.designpatterns.creational.abstractfactory;

/**
 * Concrete Product B1 (AWS Family)
 */
public class CloudWatchLogger implements AuditLogger {
    @Override
    public void log(String message) {
        System.out.println("AWS CloudWatch: [AUDIT] " + message);
    }
}
