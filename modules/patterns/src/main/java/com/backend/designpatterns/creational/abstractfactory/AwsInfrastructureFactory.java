package com.backend.designpatterns.creational.abstractfactory;

/**
 * Concrete Factory 1: AWS Ecosystem
 */
public class AwsInfrastructureFactory implements InfrastructureFactory {
    @Override
    public Storage createStorage() {
        return new S3Storage();
    }

    @Override
    public AuditLogger createLogger() {
        return new CloudWatchLogger();
    }
}
