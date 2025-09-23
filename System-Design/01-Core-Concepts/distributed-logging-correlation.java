package com.netflix.systemdesign.logging;

import org.slf4j.MDC;
import java.util.UUID;

/**
 * CorrelationContext manages a per-request correlation id backed by MDC.
 */
public final class CorrelationContext {
    public static final String CORRELATION_ID = "correlation_id";

    private CorrelationContext() {}

    public static String startOrResume(String incoming) {
        String id = incoming == null || incoming.isBlank() ? UUID.randomUUID().toString() : incoming.trim();
        MDC.put(CORRELATION_ID, id);
        return id;
    }

    public static String current() {
        return MDC.get(CORRELATION_ID);
    }

    public static void clear() {
        MDC.remove(CORRELATION_ID);
    }
}


