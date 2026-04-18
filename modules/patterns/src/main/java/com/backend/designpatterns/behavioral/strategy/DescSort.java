package com.backend.designpatterns.behavioral.strategy;

import java.util.Arrays;

// Role: Concrete Strategy
public class DescSort implements SortStrategy {
    @Override
    public int[] sort(int[] data) {
        System.out.println("Sorting DESC...");
        int[] copy = Arrays.copyOf(data, data.length);
        Arrays.sort(copy);
        // Reverse Logic
        for (int i = 0; i < copy.length / 2; i++) {
            int temp = copy[i];
            copy[i] = copy[copy.length - 1 - i];
            copy[copy.length - 1 - i] = temp;
        }
        return copy;
    }
}
