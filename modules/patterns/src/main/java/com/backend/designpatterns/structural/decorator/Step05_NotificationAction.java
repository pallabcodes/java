package com.backend.designpatterns.structural.decorator;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Step 5: FUNCTIONAL DECORATOR
 */
public interface Step05_NotificationAction extends Consumer<String> {

    static Step05_NotificationAction transform(UnaryOperator<String> transformer, Step05_NotificationAction next) {
        return message -> next.accept(transformer.apply(message));
    }
}
