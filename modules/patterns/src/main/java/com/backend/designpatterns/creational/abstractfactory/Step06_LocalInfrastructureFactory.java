package com.backend.designpatterns.creational.abstractfactory;

/**
 * Step 6: CONCRETE FACTORY (Local)
 * 
 * This factory creates the "Local" family of products.
 * It ensures that for local development, you get 
 * File Storage AND Console Logging together.
 */
public class Step06_LocalInfrastructureFactory implements Step03_InfrastructureFactory {
    @Override
    public Step01_Storage createStorage() {
        return new Step04_FileStorage();
    }

    @Override
    public Step02_AuditLogger createLogger() {
        return new Step05_ConsoleLogger();
    }
}
