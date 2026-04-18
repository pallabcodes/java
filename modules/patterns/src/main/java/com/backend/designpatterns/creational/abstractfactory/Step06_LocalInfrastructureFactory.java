package com.backend.designpatterns.creational.abstractfactory;

/**
 * Step 6: CONCRETE FACTORY (Local)
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
