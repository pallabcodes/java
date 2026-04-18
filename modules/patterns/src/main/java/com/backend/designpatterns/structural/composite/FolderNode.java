package com.backend.designpatterns.structural.composite;

import java.util.ArrayList;
import java.util.List;

// Role: Composite
public class FolderNode implements FileSystemNode {

    private final String name;
    private final List<FileSystemNode> children = new ArrayList<>();

    public FolderNode(String name) {
        this.name = name;
    }

    public void add(FileSystemNode node) {
        children.add(node);
    }

    public void remove(FileSystemNode node) {
        children.remove(node);
    }

    @Override
    public void showDetails() {
        System.out.println("Folder: " + name);
        for (FileSystemNode node : children) {
            // Indentation logic could be added here for pretty printing
            node.showDetails();
        }
    }
}
