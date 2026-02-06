package com.backend.designpatterns.structural.composite;

// Role: Leaf
public class FileNode implements FileSystemNode {

    private final String name;

    public FileNode(String name) {
        this.name = name;
    }

    @Override
    public void showDetails() {
        System.out.println("File: " + name);
    }
}
