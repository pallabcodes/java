package com.netflix.reporting.kafka;

import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.support.KafkaHeaders;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class KafkaHeadersUtil {

    public static final String CORRELATION_ID = "correlationId";
    public static final String TENANT_ID = "tenantId";

    private KafkaHeadersUtil() {}

    public static String ensureCorrelationId(Headers headers) {
        String id = get(headers, CORRELATION_ID);
        if (id == null) {
            id = UUID.randomUUID().toString();
            headers.add(CORRELATION_ID, id.getBytes(StandardCharsets.UTF_8));
        }
        return id;
    }

    public static void setTenantId(Headers headers, String tenantId) {
        if (tenantId != null) {
            headers.remove(TENANT_ID);
            headers.add(TENANT_ID, tenantId.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static String get(Headers headers, String key) {
        if (headers == null) return null;
        var h = headers.lastHeader(key);
        return h == null ? null : new String(h.value(), StandardCharsets.UTF_8);
    }
}


