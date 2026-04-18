package com.backend.designpatterns.creational.prototype;

/**
 * 🧱 L5 PROTOTYPE INTERFACE
 * 
 * We avoid 'Cloneable' (legacy Java) because it is broken/non-typesafe.
 * This generic interface ensures that every prototype provides a explicit copy() method.
 */
public interface Prototype<T> {
    /**
     * @return A deep copy of the object.
     */
    T copy();
}
