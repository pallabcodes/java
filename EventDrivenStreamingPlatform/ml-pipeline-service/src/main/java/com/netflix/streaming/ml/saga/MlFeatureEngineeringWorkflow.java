package com.netflix.streaming.ml.saga;

import com.netflix.streaming.ml.orchestrator.MlPipelineOrchestrator;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Temporal Workflow Interface for ML Feature Engineering Saga.
 *
 * This workflow orchestrates the multi-step feature engineering pipeline:
 * 1. Data Ingestion
 * 2. Data Cleaning & Validation
 * 3. Feature Extraction
 * 4. Feature Selection
 * 5. Feature Store Storage
 *
 * Each step has compensation logic for rollback on failures.
 */
@WorkflowInterface
public interface MlFeatureEngineeringWorkflow {

    /**
     * Main workflow method that orchestrates feature engineering
     */
    @WorkflowMethod
    FeatureEngineeringResult process(MlPipelineOrchestrator.FeatureEngineeringRequest request);

    // Workflow result
    class FeatureEngineeringResult {
        private final String pipelineId;
        private final String featureSetId;
        private final String featureStorePath;
        private final int featureCount;
        private final long processingTimeMs;
        private final boolean success;
        private final String errorMessage;

        public FeatureEngineeringResult(String pipelineId, String featureSetId,
                                      String featureStorePath, int featureCount,
                                      long processingTimeMs, boolean success, String errorMessage) {
            this.pipelineId = pipelineId;
            this.featureSetId = featureSetId;
            this.featureStorePath = featureStorePath;
            this.featureCount = featureCount;
            this.processingTimeMs = processingTimeMs;
            this.success = success;
            this.errorMessage = errorMessage;
        }

        // Getters
        public String getPipelineId() { return pipelineId; }
        public String getFeatureSetId() { return featureSetId; }
        public String getFeatureStorePath() { return featureStorePath; }
        public int getFeatureCount() { return featureCount; }
        public long getProcessingTimeMs() { return processingTimeMs; }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
    }
}