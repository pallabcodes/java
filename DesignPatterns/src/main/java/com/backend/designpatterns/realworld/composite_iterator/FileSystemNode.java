package com.backend.designpatterns.realworld.composite_iterator;

import java.util.Iterator;

// Component
public abstract class FileSystemNode implements Iterable<FileSystemNode> {
    protected String name;

    public FileSystemNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract void print(String indent);

    // Default iterator for Leafs (empty)
    public Iterator<FileSystemNode> iterator() {
        return new Iterator<FileSystemNode>() {
            @Override
            public boolean hasNext() { return false; }
            @Override
            public FileSystemNode next() { return null; }
        };
    }
}
