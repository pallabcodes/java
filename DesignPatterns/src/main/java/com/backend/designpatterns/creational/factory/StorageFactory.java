package com.backend.designpatterns.creational.factory;

// Role: Creator (Static Factory)
public class StorageFactory {

    public static Storage create(StorageType type) {
        return switch (type) {
            case S3 -> new S3Storage();
            case LOCAL -> new LocalStorage();
        };
    }
}
