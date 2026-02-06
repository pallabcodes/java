package com.backend.designpatterns.realworld.cloud_storage;

// Adapter for Google Cloud
public class GoogleCloudAdapter implements CloudStorage {
    private final GoogleCloudSDK gcpSdk;

    public GoogleCloudAdapter() {
        this.gcpSdk = new GoogleCloudSDK();
    }

    @Override
    public void uploadFile(String filename) {
        // Adapting the interface
        gcpSdk.upload(filename);
    }
}
