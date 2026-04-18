package com.backend.designpatterns.structural.decorator;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * 🚀 L5 FUNCTIONAL DECORATOR (Pipeline Pattern)
 * 
 * Why this is L5+:
 * 1. NO CLASS EXPLOSION: No need for SlackDecorator, SMSDecorator, etc.
 * 2. COMPOSABLE: Uses standard Java functional interfaces (Consumer, UnaryOperator).
 * 3. TRANSPARENT: The composition logic is visible at the call site, not hidden in inheritance.
 * 4. SEPARATION: Distinguishes between TRANSFORMATIONS (changing message) and ACTIONS (sending it).
 */
public interface NotificationAction extends Consumer<String> {
    
    /**
     * Helper to create a transformation decorator (e.g. Encryption, Masking).
     */
    static NotificationAction transform(UnaryOperator<String> transformer, NotificationAction next) {
        return message -> next.accept(transformer.apply(message));
    }
}
