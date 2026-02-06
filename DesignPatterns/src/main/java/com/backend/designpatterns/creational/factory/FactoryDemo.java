package com.backend.designpatterns.creational.factory;

public class FactoryDemo {

    public static void main(String[] args) {
        System.out.println("--- Factory Pattern Demo ---");

        // Use Case: Use Factory when you need to centralize object creation, 
        // decouple client from concrete classes, and select implementation based on configuration.

        // 1. Create S3 Storage
        Storage s3 = StorageFactory.create(StorageType.S3);
        System.out.println(s3.put("image.png", "binary_data"));

        // 2. Create Local Storage
        Storage local = StorageFactory.create(StorageType.LOCAL);
        System.out.println(local.put("config.json", "{...}"));
    }
}
