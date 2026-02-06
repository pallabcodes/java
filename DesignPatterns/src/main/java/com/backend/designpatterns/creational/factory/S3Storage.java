package com.backend.designpatterns.creational.factory;

// Role: Concrete Product
public class S3Storage implements Storage {
    @Override
    public String put(String key, String value) {
        return String.format("S3 Storage: Uploaded '%s' to bucket", key);
    }
}
