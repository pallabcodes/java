package com.netflix.productivity.attachment.storage;

import java.io.InputStream;

public interface StorageService {
    InputStream openStream(String storageKey);
    void save(String storageKey, InputStream data);
}

