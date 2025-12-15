package com.netflix.streaming.infrastructure.cdc;

import com.netflix.streaming.events.BaseEvent;

import java.time.Instant;
import java.util.Map;

/**
 * Generic database change event for CDC.
 * Represents a database change captured via CDC.
 */
public class DatabaseChangeEvent extends BaseEvent {

    private final String aggregateType;
    private final String aggregateId;
    private final String operation;
    private final Map<String, Object> changeData;
    private final Instant changeTimestamp;

    public DatabaseChangeEvent(String correlationId,
                              String causationId,
                              String tenantId,
                              String aggregateType,
                              String aggregateId,
                              String operation,
                              Map<String, Object> changeData,
                              Instant changeTimestamp) {
        super(correlationId, causationId, tenantId);
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.operation = operation;
        this.changeData = changeData;
        this.changeTimestamp = changeTimestamp;
    }

    @Override
    public String getAggregateId() {
        return aggregateId;
    }

    @Override
    public String getAggregateType() {
        return aggregateType;
    }

    public String getOperation() {
        return operation;
    }

    public Map<String, Object> getChangeData() {
        return changeData;
    }

    public Instant getChangeTimestamp() {
        return changeTimestamp;
    }
}

