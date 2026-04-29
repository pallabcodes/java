package com.backend.designpatterns.creational.abstractfactory;

/**
 * Step 5: CONCRETE PRODUCT B (Cloud)
 * 
 * Part of the "AWS" family. 
 * Implements logging by sending logs to AWS CloudWatch.
 */
public class Step05_CloudWatchLogger implements Step02_AuditLogger {
    @Override
    public void log(String message) {
        System.out.println("AWS CloudWatch: [AUDIT] " + message);
    }
}
