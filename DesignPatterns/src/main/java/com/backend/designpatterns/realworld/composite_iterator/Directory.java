package com.backend.designpatterns.realworld.composite_iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Composite
public class Directory extends FileSystemNode {
    private List<FileSystemNode> children = new ArrayList<>();

    public Directory(String name) {
        super(name);
    }

    public void add(FileSystemNode node) {
        children.add(node);
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + "+ Directory: " + name);
        for (FileSystemNode child : children) {
            child.print(indent + "  ");
        }
    }

    @Override
    public Iterator<FileSystemNode> iterator() {
        return children.iterator();
    }
    
    // For custom iterator access if needed
    public List<FileSystemNode> getChildren() {
        return children;
    }
}
