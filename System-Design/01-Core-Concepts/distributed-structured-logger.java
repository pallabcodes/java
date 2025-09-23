package com.netflix.systemdesign.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.StringJoiner;

/**
 * StructuredLogger emits key=value pairs including correlation id.
 */
public final class StructuredLogger {
    private final Logger logger;

    private StructuredLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public static StructuredLogger get(Class<?> clazz) {
        return new StructuredLogger(clazz);
    }

    public void info(String event, Map<String, ?> fields) {
        logAt("INFO", event, fields);
    }

    public void warn(String event, Map<String, ?> fields) {
        logAt("WARN", event, fields);
    }

    public void error(String event, Map<String, ?> fields, Throwable t) {
        String line = format(event, fields);
        logger.error(line, t);
    }

    private void logAt(String level, String event, Map<String, ?> fields) {
        String line = format(event, fields);
        switch (level) {
            case "INFO": logger.info(line); break;
            case "WARN": logger.warn(line); break;
            default: logger.info(line);
        }
    }

    private String format(String event, Map<String, ?> fields) {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("event=" + safe(event));
        String cid = CorrelationContext.current();
        if (cid != null) sj.add("correlation_id=" + safe(cid));
        if (fields != null) {
            for (Map.Entry<String, ?> e : fields.entrySet()) {
                sj.add(safe(e.getKey()) + "=" + safe(String.valueOf(e.getValue())));
            }
        }
        return sj.toString();
    }

    private String safe(String v) {
        if (v == null) return "null";
        return v.replace(' ', '_');
    }
}


