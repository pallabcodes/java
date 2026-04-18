package com.backend.designpatterns.creational.abstractfactory;

/**
 * Concrete Factory 2: Local Ecosystem
 */
public class LocalInfrastructureFactory implements InfrastructureFactory {
    @Override
    public Storage createStorage() {
        return new FileStorage();
    }

    @Override
    public AuditLogger createLogger() {
        return new ConsoleLogger();
    }
}
