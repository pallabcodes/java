package com.backend.designpatterns.realworld.cloud_storage;

// Facade (+ Strategy context via Factory)
public class CloudStorageService {

    public void upload(String filename, String provider) {
        System.out.println("Service: Starting upload for " + filename);
        
        // 1. Get Strategy (via Factory)
        CloudStorage storage = StorageFactory.getStorage(provider);
        
        // 2. Execute Strategy (via Adapter)
        storage.uploadFile(filename);
        
        System.out.println("Service: Upload completed.");
    }
}
