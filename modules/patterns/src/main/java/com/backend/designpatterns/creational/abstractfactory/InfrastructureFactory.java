package com.backend.designpatterns.creational.abstractfactory;

/**
 * THE ABSTRACT FACTORY
 * 
 * Defines factory methods for EACH product in the family.
 * Ensures that Storage and Logger always originate from the same ecosystem
 * (family).
 * 
 * The InfrastructureFactory interface defines two factory methods, and their
 * return types are the Product Interfaces (Storage and AuditLogger).
 */

public interface InfrastructureFactory {

    Storage createStorage();

    AuditLogger createLogger();
}
