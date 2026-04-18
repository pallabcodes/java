package com.backend.designpatterns.creational.factory;

// A simple enum that acts as the unique identifier for different storage implementations (S3, LOCAL). It serves as the key for the registry.

public enum StorageType {
    S3, LOCAL
}
