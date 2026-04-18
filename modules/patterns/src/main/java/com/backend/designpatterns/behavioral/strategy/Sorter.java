package com.backend.designpatterns.behavioral.strategy;

// Role: Context
public class Sorter {

    private SortStrategy strategy;

    public Sorter(SortStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(SortStrategy strategy) {
        this.strategy = strategy;
    }

    public int[] sort(int[] data) {
        return strategy.sort(data);
    }
}
