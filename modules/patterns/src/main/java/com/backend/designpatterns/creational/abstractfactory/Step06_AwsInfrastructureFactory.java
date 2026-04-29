package com.backend.designpatterns.creational.abstractfactory;

/**
 * Step 6: CONCRETE FACTORY (AWS)
 * 
 * This factory creates the "AWS" family of products.
 * It ensures that if you are in the AWS environment, you get 
 * S3 Storage AND CloudWatch Logging together.
 */
public class Step06_AwsInfrastructureFactory implements Step03_InfrastructureFactory {
    @Override
    public Step01_Storage createStorage() {
        return new Step04_S3Storage();
    }

    @Override
    public Step02_AuditLogger createLogger() {
        return new Step05_CloudWatchLogger();
    }
}
