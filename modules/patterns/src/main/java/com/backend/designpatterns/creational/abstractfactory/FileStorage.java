package com.backend.designpatterns.creational.abstractfactory;

/**
 * Concrete Product A2 (Local Family)
 */
public class FileStorage implements Storage {
    @Override
    public void persist(String data) {
        System.out.println("Local File System: Writing to file -> " + data);
    }
}
