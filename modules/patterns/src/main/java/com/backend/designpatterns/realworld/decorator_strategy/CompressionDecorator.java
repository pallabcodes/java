package com.backend.designpatterns.realworld.decorator_strategy;

// Decorator using Strategy
public class CompressionDecorator extends DataSourceDecorator {
    private final CompressionStrategy strategy;

    public CompressionDecorator(DataSource source, CompressionStrategy strategy) {
        super(source);
        this.strategy = strategy;
    }

    @Override
    public void writeData(String data) {
        String compressed = strategy.compress(data);
        System.out.println("Compressing: " + data + " -> " + compressed);
        super.writeData(compressed);
    }

    @Override
    public String readData() {
        String compressed = super.readData();
        String decompressed = strategy.decompress(compressed);
        System.out.println("Decompressing: " + compressed + " -> " + decompressed);
        return decompressed;
    }
}
