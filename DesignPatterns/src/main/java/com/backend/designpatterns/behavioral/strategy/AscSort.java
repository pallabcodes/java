package com.backend.designpatterns.behavioral.strategy;

import java.util.Arrays;

// Role: Concrete Strategy
public class AscSort implements SortStrategy {
    @Override
    public int[] sort(int[] data) {
        System.out.println("Sorting ASC...");
        int[] copy = Arrays.copyOf(data, data.length);
        Arrays.sort(copy);
        return copy;
    }
}
