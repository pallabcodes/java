package com.backend.designpatterns.creational.abstractfactory;

/**
 * Step 1: PRODUCT A (Storage Interface)
 * 
 * Part of a "Family" of related products. 
 * This defines how data should be persisted.
 */
public interface Step01_Storage {
    void persist(String data);
}
