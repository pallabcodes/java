package com.example.sharding;

import org.slf4j.MDC;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public final class RoutingDataSource extends AbstractRoutingDataSource {
    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    public static void setCurrentShardId(String shardId) {
        CONTEXT.set(shardId);
        MDC.put("shardId", shardId);
    }

    public static void clear() {
        CONTEXT.remove();
        MDC.remove("shardId");
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return CONTEXT.get();
    }
}


