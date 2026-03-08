package com.yourorg.platform.clean.usecase.port;

public interface ObjectStorePort {
    void put(String key, byte[] payload);
    byte[] get(String key);
}
