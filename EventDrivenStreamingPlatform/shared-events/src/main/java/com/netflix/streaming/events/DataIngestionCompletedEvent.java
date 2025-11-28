package com.netflix.streaming.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Domain event emitted when data ingestion is completed in ML pipeline.
 */
public class DataIngestionCompletedEvent extends BaseEvent {

    @JsonProperty("pipelineId")
    private final String pipelineId;

    @JsonProperty("rawDataPath")
    private final String rawDataPath;

    @JsonProperty("recordCount")
    private final long recordCount;

    @JsonCreator
    public DataIngestionCompletedEvent(
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("causationId") String causationId,
            @JsonProperty("tenantId") String tenantId,
            @JsonProperty("pipelineId") String pipelineId,
            @JsonProperty("rawDataPath") String rawDataPath,
            @JsonProperty("recordCount") long recordCount) {
        super(correlationId, causationId, tenantId);
        this.pipelineId = pipelineId;
        this.rawDataPath = rawDataPath;
        this.recordCount = recordCount;
    }

    @Override
    public String getAggregateId() {
        return pipelineId;
    }

    @Override
    public String getAggregateType() {
        return "MlPipeline";
    }

    public String getPipelineId() { return pipelineId; }
    public String getRawDataPath() { return rawDataPath; }
    public long getRecordCount() { return recordCount; }
}