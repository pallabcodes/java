package com.backend.designpatterns.realworld.cloud_storage;

// Adapter for AWS
public class S3Adapter implements CloudStorage {
    private final AWSS3SDK s3Sdk;

    public S3Adapter() {
        this.s3Sdk = new AWSS3SDK();
    }

    @Override
    public void uploadFile(String filename) {
        // Adapting the interface
        s3Sdk.putObject("my-app-bucket", filename);
    }
}
