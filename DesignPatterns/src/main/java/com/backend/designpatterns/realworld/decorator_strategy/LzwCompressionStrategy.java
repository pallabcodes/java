package com.backend.designpatterns.realworld.decorator_strategy;

// Concrete Strategy
public class LzwCompressionStrategy implements CompressionStrategy {
    @Override
    public String compress(String data) {
        return "[LZW_COMPRESSED]" + data;
    }

    @Override
    public String decompress(String data) {
        return data.replace("[LZW_COMPRESSED]", "");
    }
}
