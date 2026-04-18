package com.backend.designpatterns.realworld.decorator_strategy;

// Strategy Interface
public interface CompressionStrategy {
    String compress(String data);
    String decompress(String data);
}
