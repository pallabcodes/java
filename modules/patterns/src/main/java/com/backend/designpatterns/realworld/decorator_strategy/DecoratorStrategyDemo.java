package com.backend.designpatterns.realworld.decorator_strategy;

public class DecoratorStrategyDemo {

    public static void main(String[] args) {
        
        String salaryRecords = "Name,Salary\nJohn,1000\nJane,2000";

        // 1. Create Data Source (Component)
        DataSource source = new FileDataSource("salaries.txt");

        // 2. Wrap with Compression (using GZIP Strategy)
        // Decorator + Client chooses Strategy
        source = new CompressionDecorator(source, new GzipCompressionStrategy());

        // 3. Wrap with Encryption
        source = new EncryptionDecorator(source);
        
        // Write
        System.out.println("--- WRITING ---");
        source.writeData(salaryRecords);

        // Read
        System.out.println("\n--- READING ---");
        System.out.println(source.readData());
    }
}
