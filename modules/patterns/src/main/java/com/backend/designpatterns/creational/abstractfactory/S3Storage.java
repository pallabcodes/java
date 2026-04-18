package com.backend.designpatterns.creational.abstractfactory;

/**
 * Concrete Product A1 (AWS Family)
 */
public class S3Storage implements Storage {
    @Override
    public void persist(String data) {
        System.out.println("AWS S3: Persisting data to bucket -> " + data);
    }
}
