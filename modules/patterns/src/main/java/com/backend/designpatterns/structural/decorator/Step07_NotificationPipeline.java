package com.backend.designpatterns.structural.decorator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Step 7: PIPELINE CORE
 */
public final class Step07_NotificationPipeline {
    
    private final List<Step05_NotificationAction> actions = new ArrayList<>();
    private final List<UnaryOperator<String>> transformers = new ArrayList<>();

    public static Step07_NotificationPipeline builder() {
        return new Step07_NotificationPipeline();
    }

    public Step07_NotificationPipeline addTransformer(UnaryOperator<String> transformer) {
        this.transformers.add(transformer);
        return this;
    }

    public Step07_NotificationPipeline addAction(Step05_NotificationAction action) {
        this.actions.add(action);
        return this;
    }

    public Step07_NotificationPipeline build() {
        return this;
    }

    public void send(String rawMessage) {
        String processedMessage = rawMessage;
        for (var transformer : transformers) {
            processedMessage = transformer.apply(processedMessage);
        }

        for (var action : actions) {
            action.accept(processedMessage);
        }
    }
}
