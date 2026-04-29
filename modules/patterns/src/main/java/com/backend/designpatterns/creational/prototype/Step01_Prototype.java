package com.backend.designpatterns.creational.prototype;

/**
 * Step 1: THE PROTOTYPE CONTRACT
 * 
 * Defines the ability to "Clone" or "Copy" oneself. 
 * This allows creating new objects by copying an existing instance
 * instead of using the 'new' keyword.
 */
public interface Step01_Prototype<T> {
    T copy();
}
