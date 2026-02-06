package com.backend.designpatterns.realworld.cloud_storage;

// Adaptee 2 (Simulated SDK)
public class GoogleCloudSDK {
    public void upload(String blobName) {
        System.out.println("Google Cloud: Uploading blob " + blobName);
    }
}
