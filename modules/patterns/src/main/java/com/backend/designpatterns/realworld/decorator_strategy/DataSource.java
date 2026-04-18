package com.backend.designpatterns.realworld.decorator_strategy;

// Component Interface
public interface DataSource {
    void writeData(String data);
    String readData();
}
