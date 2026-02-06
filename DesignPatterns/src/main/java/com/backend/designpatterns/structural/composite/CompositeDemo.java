package com.backend.designpatterns.structural.composite;

public class CompositeDemo {

    public static void main(String[] args) {
        System.out.println("--- Composite Pattern Demo ---");

        // Use Case: Use Composite when you need to represent part-whole hierarchies (trees) 
        // and treat individual objects and compositions uniformly (e.g., File System, UI Layouts).

        FolderNode root = new FolderNode("root");
        root.add(new FileNode("config.xml"));

        FolderNode images = new FolderNode("images");
        images.add(new FileNode("logo.png"));
        images.add(new FileNode("banner.jpg"));

        root.add(images);

        // Recursive display
        root.showDetails();
    }
}
