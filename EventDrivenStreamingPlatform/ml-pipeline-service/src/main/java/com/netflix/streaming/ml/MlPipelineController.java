package com.netflix.streaming.ml;

import com.netflix.streaming.ml.orchestrator.MlPipelineOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * REST API Controller for ML Pipeline Operations.
 *
 * Provides endpoints for:
 * - Starting ML pipelines (feature engineering, training, deployment)
 * - Monitoring pipeline status
 * - Managing pipeline lifecycle
 * - Retrieving pipeline results
 */
@RestController
@RequestMapping("/api/v1/ml/pipelines")
@Tag(name = "ML Pipeline API", description = "Machine learning pipeline orchestration and monitoring")
public class MlPipelineController {

    private static final Logger logger = LoggerFactory.getLogger(MlPipelineController.class);

    private final MlPipelineOrchestrator orchestrator;

    public MlPipelineController(MlPipelineOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    /**
     * Start Feature Engineering Pipeline
     */
    @PostMapping("/feature-engineering")
    @Operation(summary = "Start feature engineering pipeline",
               description = "Initiates a feature engineering saga workflow for ML data preparation")
    public ResponseEntity<PipelineResponse> startFeatureEngineering(
            @Valid @RequestBody FeatureEngineeringRequest request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {

        if (correlationId == null) {
            correlationId = java.util.UUID.randomUUID().toString();
        }

        logger.info("Starting feature engineering pipeline with correlation ID: {}", correlationId);

        try {
            var orchestratorRequest = new MlPipelineOrchestrator.FeatureEngineeringRequest(
                request.getPipelineId(),
                request.getDataSource(),
                request.getFeatureConfig()
            );

            String workflowId = orchestrator.startFeatureEngineeringPipeline(orchestratorRequest);

            return ResponseEntity.accepted().body(new PipelineResponse(
                workflowId,
                "FEATURE_ENGINEERING",
                "STARTED",
                "Feature engineering pipeline initiated"
            ));

        } catch (Exception e) {
            logger.error("Failed to start feature engineering pipeline", e);
            return ResponseEntity.internalServerError().body(new PipelineResponse(
                null,
                "FEATURE_ENGINEERING",
                "FAILED",
                "Failed to start pipeline: " + e.getMessage()
            ));
        }
    }

    /**
     * Start Model Training Pipeline
     */
    @PostMapping("/model-training")
    @Operation(summary = "Start model training pipeline",
               description = "Initiates a model training saga workflow")
    public ResponseEntity<PipelineResponse> startModelTraining(
            @Valid @RequestBody ModelTrainingRequest request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {

        if (correlationId == null) {
            correlationId = java.util.UUID.randomUUID().toString();
        }

        logger.info("Starting model training pipeline: {}", request.getPipelineId());

        try {
            var orchestratorRequest = new MlPipelineOrchestrator.ModelTrainingRequest(
                request.getPipelineId(),
                request.getFeaturePipelineId(),
                request.getModelType(),
                request.getTrainingConfig()
            );

            String workflowId = orchestrator.startModelTrainingPipeline(orchestratorRequest);

            return ResponseEntity.accepted().body(new PipelineResponse(
                workflowId,
                "MODEL_TRAINING",
                "STARTED",
                "Model training pipeline initiated"
            ));

        } catch (Exception e) {
            logger.error("Failed to start model training pipeline", e);
            return ResponseEntity.internalServerError().body(new PipelineResponse(
                null,
                "MODEL_TRAINING",
                "FAILED",
                "Failed to start pipeline: " + e.getMessage()
            ));
        }
    }

    /**
     * Start Complete ML Pipeline
     */
    @PostMapping("/complete")
    @Operation(summary = "Start complete ML pipeline",
               description = "Initiates full ML pipeline: feature engineering → training → deployment")
    public ResponseEntity<PipelineResponse> startCompletePipeline(
            @Valid @RequestBody CompleteMlPipelineRequest request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {

        if (correlationId == null) {
            correlationId = java.util.UUID.randomUUID().toString();
        }

        logger.info("Starting complete ML pipeline: {}", request.getPipelineId());

        try {
            var orchestratorRequest = new MlPipelineOrchestrator.CompleteMlPipelineRequest(
                request.getPipelineId(),
                request.getDataSource(),
                request.getModelType(),
                request.getTargetEnvironment(),
                request.getFeatureConfig(),
                request.getTrainingConfig(),
                request.getDeploymentConfig()
            );

            String pipelineId = orchestrator.startCompleteMlPipeline(orchestratorRequest);

            return ResponseEntity.accepted().body(new PipelineResponse(
                pipelineId,
                "COMPLETE_ML_PIPELINE",
                "STARTED",
                "Complete ML pipeline initiated with feature engineering, training, and deployment"
            ));

        } catch (Exception e) {
            logger.error("Failed to start complete ML pipeline", e);
            return ResponseEntity.internalServerError().body(new PipelineResponse(
                null,
                "COMPLETE_ML_PIPELINE",
                "FAILED",
                "Failed to start pipeline: " + e.getMessage()
            ));
        }
    }

    /**
     * Get Pipeline Status
     */
    @GetMapping("/{workflowId}/status")
    @Operation(summary = "Get pipeline status",
               description = "Retrieves the current status of a running pipeline")
    public ResponseEntity<MlPipelineOrchestrator.PipelineStatus> getPipelineStatus(
            @PathVariable String workflowId) {

        logger.debug("Getting status for pipeline: {}", workflowId);

        try {
            MlPipelineOrchestrator.PipelineStatus status = orchestrator.getPipelineStatus(workflowId);
            return ResponseEntity.ok(status);

        } catch (Exception e) {
            logger.error("Failed to get pipeline status for: {}", workflowId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Cancel Pipeline
     */
    @DeleteMapping("/{workflowId}")
    @Operation(summary = "Cancel pipeline",
               description = "Cancels a running pipeline and triggers compensation")
    public ResponseEntity<ApiResponse> cancelPipeline(@PathVariable String workflowId) {

        logger.info("Cancelling pipeline: {}", workflowId);

        try {
            orchestrator.cancelPipeline(workflowId);
            return ResponseEntity.ok(new ApiResponse(
                "CANCELLED",
                "Pipeline cancellation initiated"
            ));

        } catch (Exception e) {
            logger.error("Failed to cancel pipeline: {}", workflowId, e);
            return ResponseEntity.internalServerError().body(new ApiResponse(
                "CANCEL_FAILED",
                "Failed to cancel pipeline: " + e.getMessage()
            ));
        }
    }

    /**
     * Get Pipeline Statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "Get pipeline statistics",
               description = "Retrieves overall ML pipeline system statistics")
    public ResponseEntity<Map<String, Object>> getPipelineStats() {

        logger.debug("Retrieving ML pipeline statistics");

        // In production, this would aggregate from various sources
        Map<String, Object> stats = new HashMap<>();
        stats.put("activePipelines", 5); // Mock data
        stats.put("completedToday", 23);
        stats.put("failedToday", 2);
        stats.put("avgProcessingTime", "45 minutes");
        stats.put("successRate", "92%");

        return ResponseEntity.ok(stats);
    }

    // Request DTOs

    public static class FeatureEngineeringRequest {
        private String pipelineId = java.util.UUID.randomUUID().toString();
        private String dataSource = "analytics-service";
        private Map<String, Object> featureConfig = new HashMap<>();

        // Getters and setters
        public String getPipelineId() { return pipelineId; }
        public void setPipelineId(String pipelineId) { this.pipelineId = pipelineId; }
        public String getDataSource() { return dataSource; }
        public void setDataSource(String dataSource) { this.dataSource = dataSource; }
        public Map<String, Object> getFeatureConfig() { return featureConfig; }
        public void setFeatureConfig(Map<String, Object> featureConfig) { this.featureConfig = featureConfig; }
    }

    public static class ModelTrainingRequest {
        private String pipelineId = java.util.UUID.randomUUID().toString();
        private String featurePipelineId;
        private String modelType = "recommendation";
        private Map<String, Object> trainingConfig = new HashMap<>();

        // Getters and setters
        public String getPipelineId() { return pipelineId; }
        public void setPipelineId(String pipelineId) { this.pipelineId = pipelineId; }
        public String getFeaturePipelineId() { return featurePipelineId; }
        public void setFeaturePipelineId(String featurePipelineId) { this.featurePipelineId = featurePipelineId; }
        public String getModelType() { return modelType; }
        public void setModelType(String modelType) { this.modelType = modelType; }
        public Map<String, Object> getTrainingConfig() { return trainingConfig; }
        public void setTrainingConfig(Map<String, Object> trainingConfig) { this.trainingConfig = trainingConfig; }
    }

    public static class CompleteMlPipelineRequest {
        private String pipelineId = java.util.UUID.randomUUID().toString();
        private String dataSource = "analytics-service";
        private String modelType = "recommendation";
        private String targetEnvironment = "staging";
        private Map<String, Object> featureConfig = new HashMap<>();
        private Map<String, Object> trainingConfig = new HashMap<>();
        private Map<String, Object> deploymentConfig = new HashMap<>();

        // Getters and setters
        public String getPipelineId() { return pipelineId; }
        public void setPipelineId(String pipelineId) { this.pipelineId = pipelineId; }
        public String getDataSource() { return dataSource; }
        public void setDataSource(String dataSource) { this.dataSource = dataSource; }
        public String getModelType() { return modelType; }
        public void setModelType(String modelType) { this.modelType = modelType; }
        public String getTargetEnvironment() { return targetEnvironment; }
        public void setTargetEnvironment(String targetEnvironment) { this.targetEnvironment = targetEnvironment; }
        public Map<String, Object> getFeatureConfig() { return featureConfig; }
        public void setFeatureConfig(Map<String, Object> featureConfig) { this.featureConfig = featureConfig; }
        public Map<String, Object> getTrainingConfig() { return trainingConfig; }
        public void setTrainingConfig(Map<String, Object> trainingConfig) { this.trainingConfig = trainingConfig; }
        public Map<String, Object> getDeploymentConfig() { return deploymentConfig; }
        public void setDeploymentConfig(Map<String, Object> deploymentConfig) { this.deploymentConfig = deploymentConfig; }
    }

    // Response DTOs

    public static class PipelineResponse {
        public final String workflowId;
        public final String pipelineType;
        public final String status;
        public final String message;

        public PipelineResponse(String workflowId, String pipelineType, String status, String message) {
            this.workflowId = workflowId;
            this.pipelineType = pipelineType;
            this.status = status;
            this.message = message;
        }
    }

    public static class ApiResponse {
        public final String status;
        public final String message;

        public ApiResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }
    }
}