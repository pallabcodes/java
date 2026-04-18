package com.backend.designpatterns.behavioral.strategy;

// Role: Factory (Helper)
public class SortStrategyFactory {

    public static SortStrategy get(SortType type) {
        return switch (type) {
            case ASC -> new AscSort();
            case DESC -> new DescSort();
        };
    }
}
