package com.backend.designpatterns.realworld.decorator_strategy;

// Decorator
public class EncryptionDecorator extends DataSourceDecorator {

    public EncryptionDecorator(DataSource source) {
        super(source);
    }

    @Override
    public void writeData(String data) {
        String encrypted = encrypt(data);
        System.out.println("Encrypting: " + data + " -> " + encrypted);
        super.writeData(encrypted);
    }

    @Override
    public String readData() {
        String encrypted = super.readData();
        String decrypted = decrypt(encrypted);
        System.out.println("Decrypting: " + encrypted + " -> " + decrypted);
        return decrypted;
    }

    private String encrypt(String data) {
        return "{ENCRYPTED}" + data + "{/ENCRYPTED}";
    }

    private String decrypt(String data) {
        return data.replace("{ENCRYPTED}", "").replace("{/ENCRYPTED}", "");
    }
}
