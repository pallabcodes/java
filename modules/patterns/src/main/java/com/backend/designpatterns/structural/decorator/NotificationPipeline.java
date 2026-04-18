package com.backend.designpatterns.structural.decorator;

import java.util.ArrayList;
import java.util.List;

/**
 * THE PIPELINE CORE
 * 
 * Aggregates multiple actions and executes them as a single 'Notifier'.
 */
public final class NotificationPipeline {
    
    private final List<NotificationAction> actions = new ArrayList<>();
    private final List<java.util.function.UnaryOperator<String>> transformers = new ArrayList<>();

    public static NotificationPipeline builder() {
        return new NotificationPipeline();
    }

    public NotificationPipeline addTransformer(java.util.function.UnaryOperator<String> transformer) {
        this.transformers.add(transformer);
        return this;
    }

    public NotificationPipeline addAction(NotificationAction action) {
        this.actions.add(action);
        return this;
    }

    public NotificationPipeline build() {
        return this;
    }

    /**
     * Executes the pipeline: Transforms first, then executes all actions.
     */
    public void send(String rawMessage) {
        // 1. Apply all transformations in order (Decoration of the data)
        String processedMessage = rawMessage;
        for (var transformer : transformers) {
            processedMessage = transformer.apply(processedMessage);
        }

        // 2. Execute all actions (Decoration of the behavior)
        for (var action : actions) {
            action.accept(processedMessage);
        }
    }
}
