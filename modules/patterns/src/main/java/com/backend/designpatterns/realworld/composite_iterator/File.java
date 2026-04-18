package com.backend.designpatterns.realworld.composite_iterator;

// Leaf
public class File extends FileSystemNode {

    public File(String name) {
        super(name);
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + "- File: " + name);
    }
}
