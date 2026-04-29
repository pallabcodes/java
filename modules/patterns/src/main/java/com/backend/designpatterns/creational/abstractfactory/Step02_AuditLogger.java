package com.backend.designpatterns.creational.abstractfactory;

/**
 * Step 2: PRODUCT B (Audit Logger Interface)
 * 
 * Another part of the "Family" of related products. 
 * This defines how actions should be logged.
 */
public interface Step02_AuditLogger {
    void log(String message);
}
