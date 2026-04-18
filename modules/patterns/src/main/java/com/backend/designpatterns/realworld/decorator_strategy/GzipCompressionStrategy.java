package com.backend.designpatterns.realworld.decorator_strategy;

// Concrete Strategy
public class GzipCompressionStrategy implements CompressionStrategy {
    @Override
    public String compress(String data) {
        return "[GZIP_COMPRESSED]" + data;
    }

    @Override
    public String decompress(String data) {
        return data.replace("[GZIP_COMPRESSED]", "");
    }
}
