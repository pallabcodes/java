package com.backend.designpatterns.behavioral.strategy;

import java.util.Arrays;

public class StrategyDemo {

    public static void main(String[] args) {
        System.out.println("--- Strategy Pattern Demo ---");

        // Use Case: Use Strategy when you want to define a family of algorithms, encapsulate each one, 
        // and make them interchangeable at runtime (e.g., Sorting methods, Payment gateways).
        
        int[] data = {5, 1, 9, 3};

        // 1. Get Strategy from Factory
        SortStrategy strategy = SortStrategyFactory.get(SortType.DESC);

        // 2. Inject into Context
        Sorter sorter = new Sorter(strategy);
        
        int[] result = sorter.sort(data);
        System.out.println("Result: " + Arrays.toString(result));

        // 3. Switch Strategy Runtime
        sorter.setStrategy(SortStrategyFactory.get(SortType.ASC));
        System.out.println("Switched to ASC: " + Arrays.toString(sorter.sort(data)));
    }
}
