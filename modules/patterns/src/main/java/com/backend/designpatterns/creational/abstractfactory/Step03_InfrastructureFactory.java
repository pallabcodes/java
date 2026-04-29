package com.backend.designpatterns.creational.abstractfactory;

/**
 * Step 3: THE ABSTRACT FACTORY (Interface)
 * 
 * Problem: How do we create families of related objects (Storage + Logger) 
 * without being tied to a specific environment (Local vs AWS)?
 * 
 * Solution: Abstract Factory.
 * 
 * It defines methods to create each product in the family.
 * The concrete factories will decide WHICH versions (AWS or Local) to create.
 */
public interface Step03_InfrastructureFactory {

    Step01_Storage createStorage();

    Step02_AuditLogger createLogger();
}
