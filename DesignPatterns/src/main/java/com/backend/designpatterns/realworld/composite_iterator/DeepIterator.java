package com.backend.designpatterns.realworld.composite_iterator;

import java.util.Iterator;
import java.util.Stack;

// Custom Iterator for Deep Traversal
public class DeepIterator implements Iterator<FileSystemNode> {
    
    private Stack<Iterator<FileSystemNode>> stack = new Stack<>();

    public DeepIterator(Iterator<FileSystemNode> iterator) {
        stack.push(iterator);
    }

    @Override
    public boolean hasNext() {
        if (stack.isEmpty()) {
            return false;
        } else {
            Iterator<FileSystemNode> iterator = stack.peek();
            if (!iterator.hasNext()) {
                stack.pop();
                return hasNext();
            } else {
                return true;
            }
        }
    }

    @Override
    public FileSystemNode next() {
        if (hasNext()) {
            Iterator<FileSystemNode> iterator = stack.peek();
            FileSystemNode component = iterator.next();
            
            // If component is a Directory, push its iterator to stack
            if (component instanceof Directory) {
                stack.push(component.iterator());
            }
            return component;
        } else {
            return null;
        }
    }
}
