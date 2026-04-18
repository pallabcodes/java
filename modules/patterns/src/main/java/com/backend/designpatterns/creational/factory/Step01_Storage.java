package com.backend.designpatterns.creational.factory;

/**
 * Step 1: PRODUCT INTERFACE (Abstract Product)
 * 
 * In a professional system, the client only interacts with this interface.
 */
public interface Step01_Storage {
    String name();
    String put(String key, String value);
}
