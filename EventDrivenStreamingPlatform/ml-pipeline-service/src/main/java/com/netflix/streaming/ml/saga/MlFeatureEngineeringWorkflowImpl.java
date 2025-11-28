package com.netflix.streaming.ml.saga;

import com.netflix.streaming.ml.orchestrator.MlPipelineOrchestrator;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;

/**
 * Temporal Workflow Implementation for ML Feature Engineering Saga.
 *
 * Demonstrates Saga Pattern with:
 * - Orchestrated workflow steps
 * - Compensation logic for rollbacks
 * - Event-driven communication between steps
 * - Fault tolerance and retry logic
 */
public class MlFeatureEngineeringWorkflowImpl implements MlFeatureEngineeringWorkflow {

    private static final Logger logger = Workflow.getLogger(MlFeatureEngineeringWorkflowImpl.class);

    // Activity stubs with retry and timeout configuration
    private final FeatureEngineeringActivities activities = Workflow.newActivityStub(
        FeatureEngineeringActivities.class,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(30))
            .setRetryOptions(RetryOptions.newBuilder()
                .setInitialInterval(Duration.ofSeconds(5))
                .setMaximumInterval(Duration.ofMinutes(5))
                .setBackoffCoefficient(2.0)
                .setMaximumAttempts(3)
                .build())
            .build()
    );

    // Compensation tracking
    private boolean dataIngested = false;
    private boolean dataCleaned = false;
    private boolean featuresExtracted = false;
    private boolean featuresStored = false;

    @Override
    public FeatureEngineeringResult process(MlPipelineOrchestrator.FeatureEngineeringRequest request) {
        String pipelineId = request.getPipelineId();
        long startTime = Workflow.currentTimeMillis();

        logger.info("Starting feature engineering saga for pipeline: {}", pipelineId);

        try {
            // Step 1: Data Ingestion
            logger.info("Step 1: Data ingestion for pipeline: {}", pipelineId);
            DataIngestionResult ingestionResult = activities.ingestData(request);
            dataIngested = true;

            // Step 2: Data Cleaning & Validation
            logger.info("Step 2: Data cleaning for pipeline: {}", pipelineId);
            DataCleaningResult cleaningResult = activities.cleanData(ingestionResult);
            dataCleaned = true;

            // Step 3: Feature Extraction
            logger.info("Step 3: Feature extraction for pipeline: {}", pipelineId);
            FeatureExtractionResult extractionResult = activities.extractFeatures(cleaningResult);
            featuresExtracted = true;

            // Step 4: Feature Selection & Optimization
            logger.info("Step 4: Feature selection for pipeline: {}", pipelineId);
            FeatureSelectionResult selectionResult = activities.selectFeatures(extractionResult);

            // Step 5: Feature Store Storage
            logger.info("Step 5: Feature storage for pipeline: {}", pipelineId);
            FeatureStorageResult storageResult = activities.storeFeatures(selectionResult);
            featuresStored = true;

            long processingTime = Workflow.currentTimeMillis() - startTime;

            logger.info("Feature engineering saga completed successfully for pipeline: {}", pipelineId);

            return new FeatureEngineeringResult(
                pipelineId,
                storageResult.getFeatureSetId(),
                storageResult.getFeatureStorePath(),
                selectionResult.getSelectedFeatureCount(),
                processingTime,
                true,
                null
            );

        } catch (Exception e) {
            logger.error("Feature engineering saga failed for pipeline: {}, compensating...", pipelineId, e);

            // Execute compensation logic (Saga compensation)
            compensate(pipelineId, e);

            long processingTime = Workflow.currentTimeMillis() - startTime;

            return new FeatureEngineeringResult(
                pipelineId,
                null,
                null,
                0,
                processingTime,
                false,
                e.getMessage()
            );
        }
    }

    /**
     * Saga Compensation Logic - Rollback completed steps in reverse order
     */
    private void compensate(String pipelineId, Exception originalError) {
        logger.warn("Executing compensation for feature engineering pipeline: {}", pipelineId);

        try {
            // Compensate in reverse order
            if (featuresStored) {
                logger.info("Compensating: Removing stored features for pipeline: {}", pipelineId);
                activities.removeStoredFeatures(pipelineId);
            }

            if (featuresExtracted) {
                logger.info("Compensating: Cleaning extracted features for pipeline: {}", pipelineId);
                activities.cleanupExtractedFeatures(pipelineId);
            }

            if (dataCleaned) {
                logger.info("Compensating: Removing cleaned data for pipeline: {}", pipelineId);
                activities.removeCleanedData(pipelineId);
            }

            if (dataIngested) {
                logger.info("Compensating: Removing ingested data for pipeline: {}", pipelineId);
                activities.removeIngestedData(pipelineId);
            }

            logger.info("Compensation completed for pipeline: {}", pipelineId);

        } catch (Exception compensationError) {
            logger.error("Compensation failed for pipeline: {}, original error: {}, compensation error: {}",
                        pipelineId, originalError.getMessage(), compensationError.getMessage());
            // In production, you might want to emit compensation failure events
        }
    }

    // Activity result classes (simplified for demo)
    public static class DataIngestionResult {
        private final String rawDataPath;
        private final long recordCount;

        public DataIngestionResult(String rawDataPath, long recordCount) {
            this.rawDataPath = rawDataPath;
            this.recordCount = recordCount;
        }

        public String getRawDataPath() { return rawDataPath; }
        public long getRecordCount() { return recordCount; }
    }

    public static class DataCleaningResult {
        private final String cleanedDataPath;
        private final long validRecordCount;
        private final long invalidRecordCount;

        public DataCleaningResult(String cleanedDataPath, long validRecordCount, long invalidRecordCount) {
            this.cleanedDataPath = cleanedDataPath;
            this.validRecordCount = validRecordCount;
            this.invalidRecordCount = invalidRecordCount;
        }

        public String getCleanedDataPath() { return cleanedDataPath; }
        public long getValidRecordCount() { return validRecordCount; }
        public long getInvalidRecordCount() { return invalidRecordCount; }
    }

    public static class FeatureExtractionResult {
        private final String extractedFeaturesPath;
        private final int rawFeatureCount;

        public FeatureExtractionResult(String extractedFeaturesPath, int rawFeatureCount) {
            this.extractedFeaturesPath = extractedFeaturesPath;
            this.rawFeatureCount = rawFeatureCount;
        }

        public String getExtractedFeaturesPath() { return extractedFeaturesPath; }
        public int getRawFeatureCount() { return rawFeatureCount; }
    }

    public static class FeatureSelectionResult {
        private final String selectedFeaturesPath;
        private final int selectedFeatureCount;

        public FeatureSelectionResult(String selectedFeaturesPath, int selectedFeatureCount) {
            this.selectedFeaturesPath = selectedFeaturesPath;
            this.selectedFeatureCount = selectedFeatureCount;
        }

        public String getSelectedFeaturesPath() { return selectedFeaturesPath; }
        public int getSelectedFeatureCount() { return selectedFeatureCount; }
    }

    public static class FeatureStorageResult {
        private final String featureSetId;
        private final String featureStorePath;

        public FeatureStorageResult(String featureSetId, String featureStorePath) {
            this.featureSetId = featureSetId;
            this.featureStorePath = featureStorePath;
        }

        public String getFeatureSetId() { return featureSetId; }
        public String getFeatureStorePath() { return featureStorePath; }
    }
}