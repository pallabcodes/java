package com.backend.designpatterns.creational.abstractfactory;

/**
 * Step 4: CONCRETE PRODUCT A (Cloud)
 */
public class Step04_S3Storage implements Step01_Storage {
    @Override
    public void persist(String data) {
        System.out.println("AWS S3: Persisting data to bucket -> " + data);
    }
}
