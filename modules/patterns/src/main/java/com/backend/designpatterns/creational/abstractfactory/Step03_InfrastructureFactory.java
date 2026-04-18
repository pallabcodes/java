package com.backend.designpatterns.creational.abstractfactory;

/**
 * Step 3: THE ABSTRACT FACTORY
 * 
 * Defines factory methods for EACH product in the family.
 */
public interface Step03_InfrastructureFactory {

    Step01_Storage createStorage();

    Step02_AuditLogger createLogger();
}
