package com.backend.designpatterns.realworld.cloud_storage;

// Adaptee 1 (Simulated SDK)
public class AWSS3SDK {
    public void putObject(String bucket, String key) {
        System.out.println("AWS S3: Uploading " + key + " to bucket " + bucket);
    }
}
