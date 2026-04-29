package com.backend.designpatterns.creational.factory;

/**
 * Step 1: PRODUCT INTERFACE (Abstract Product)
 * 
 * In a professional system, the client only interacts with this interface.
 * 
 * Why?
 * - Decoupling: The client doesn't care if it's S3, Disk, or Cloud.
 * - Swapability: You can change the storage provider without breaking any other code.
 * - Consistency: Every storage type MUST implement these core behaviors.
 */
public interface Step01_Storage {
    String name();
    String put(String key, String value);
}
