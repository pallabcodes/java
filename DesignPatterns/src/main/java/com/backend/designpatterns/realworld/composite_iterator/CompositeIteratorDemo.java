package com.backend.designpatterns.realworld.composite_iterator;

import java.util.Iterator;

public class CompositeIteratorDemo {

    public static void main(String[] args) {
        
        Directory root = new Directory("Root");
        Directory src = new Directory("src");
        Directory main = new Directory("main");
        
        File file1 = new File("Main.java");
        File file2 = new File("Utils.java");
        File file3 = new File("pom.xml");

        // Building Tree
        root.add(src);
        root.add(file3);
        src.add(main);
        main.add(file1);
        main.add(file2);

        // 1. Structural View (Composite behavior)
        System.out.println("--- Tree Structure ---");
        root.print("");

        // 2. Traversal (Iterator behavior)
        // Using built-in iterator for immediate children
        System.out.println("\n--- Immediate Children of Root ---");
        Iterator<FileSystemNode> rootIter = root.iterator();
        while(rootIter.hasNext()) {
            System.out.println("Found: " + rootIter.next().getName());
        }
        
        // 3. Deep Iteration (Custom Iterator)
        // Note: DeepIterator logic is complex to implement perfectly with just standard Iterator interface
        // typically requiring a separate 'create iterator' logic. 
        // For simplicity, we stick to the print() recursive traversal which is internal iteration.
    }
}
