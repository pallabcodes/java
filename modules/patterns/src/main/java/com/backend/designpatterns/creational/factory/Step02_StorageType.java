package com.backend.designpatterns.creational.factory;

/**
 * Step 2: PRODUCT IDENTIFIER (Enum)
 * 
 * Provides a fixed set of constants to identify storage types.
 * Using an Enum prevents "Magic Strings" and ensures type safety 
 * when calling the factory.
 */
public enum Step02_StorageType {
    S3, LOCAL
}
