package com.backend.designpatterns.creational.abstractfactory;

/**
 * Step 4: CONCRETE PRODUCT A (Local)
 * 
 * Part of the "Local" family. 
 * Implements storage by writing to a local file.
 */
public class Step04_FileStorage implements Step01_Storage {
    @Override
    public void persist(String data) {
        System.out.println("Local File System: Writing to file -> " + data);
    }
}
