package com.backend.designpatterns.realworld.decorator_strategy;

// Concrete Component
public class FileDataSource implements DataSource {
    private final String filename;
    private String data; // Simulating file storage in memory

    public FileDataSource(String filename) {
        this.filename = filename;
    }

    @Override
    public void writeData(String data) {
        System.out.println("Writing to file (" + filename + "): " + data);
        this.data = data;
    }

    @Override
    public String readData() {
        System.out.println("Reading from file (" + filename + "): " + data);
        return data;
    }
}
