package com.netflix.attachments.storage;

import java.io.InputStream;

public interface StorageService {
    void putObject(String storageKey, InputStream data, long size, String contentType);
    InputStream getObject(String storageKey);
    void deleteObject(String storageKey);
    boolean exists(String storageKey);
}
