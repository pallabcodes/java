package com.netflix.streaming.ml.orchestrator;

import com.netflix.streaming.events.*;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * ML Pipeline Orchestrator using Saga Pattern.
 *
 * This demonstrates:
 * - Saga Pattern: Distributed transaction coordination across ML pipeline steps
 * - Orchestration vs Choreography: Centralized workflow coordination
 * - Compensation Logic: Rollback failed pipeline steps
 * - Event-Driven Integration: Saga steps emit domain events
 * - Temporal Workflows: Durable, long-running saga execution
 */
@Service
public class MlPipelineOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(MlPipelineOrchestrator.class);

    private final WorkflowClient workflowClient;
    private final Tracer tracer;

    public MlPipelineOrchestrator(WorkflowClient workflowClient, Tracer tracer) {
        this.workflowClient = workflowClient;
        this.tracer = tracer;
    }

    /**
     * Start ML Feature Engineering Pipeline
     */
    public String startFeatureEngineeringPipeline(FeatureEngineeringRequest request) {
        Span span = tracer.spanBuilder("ml.orchestrate.feature-engineering")
            .setAttribute("pipeline.id", request.getPipelineId())
            .setAttribute("data.source", request.getDataSource())
            .startSpan();

        try {
            logger.info("Starting feature engineering pipeline: {}", request.getPipelineId());

            // Start Temporal workflow for feature engineering saga
            MlFeatureEngineeringWorkflow workflow = workflowClient.newWorkflowStub(
                MlFeatureEngineeringWorkflow.class,
                WorkflowOptions.newBuilder()
                    .setTaskQueue("ml-pipeline-task-queue")
                    .setWorkflowId("feature-eng-" + request.getPipelineId())
                    .setWorkflowExecutionTimeout(Duration.ofHours(4))
                    .build()
            );

            // Start the workflow asynchronously
            WorkflowClient.start(workflow::process, request);

            span.setStatus(StatusCode.OK);
            logger.info("Feature engineering pipeline started: {}", request.getPipelineId());

            return "feature-eng-" + request.getPipelineId();

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Failed to start feature engineering pipeline: {}", request.getPipelineId(), e);
            throw new MlPipelineException("Failed to start feature engineering pipeline", e);
        } finally {
            span.end();
        }
    }

    /**
     * Start ML Model Training Pipeline
     */
    public String startModelTrainingPipeline(ModelTrainingRequest request) {
        Span span = tracer.spanBuilder("ml.orchestrate.model-training")
            .setAttribute("pipeline.id", request.getPipelineId())
            .setAttribute("model.type", request.getModelType())
            .startSpan();

        try {
            logger.info("Starting model training pipeline: {}", request.getPipelineId());

            MlModelTrainingWorkflow workflow = workflowClient.newWorkflowStub(
                MlModelTrainingWorkflow.class,
                WorkflowOptions.newBuilder()
                    .setTaskQueue("ml-pipeline-task-queue")
                    .setWorkflowId("model-train-" + request.getPipelineId())
                    .setWorkflowExecutionTimeout(Duration.ofHours(24)) // Long-running training
                    .build()
            );

            WorkflowClient.start(workflow::train, request);

            span.setStatus(StatusCode.OK);
            logger.info("Model training pipeline started: {}", request.getPipelineId());

            return "model-train-" + request.getPipelineId();

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Failed to start model training pipeline: {}", request.getPipelineId(), e);
            throw new MlPipelineException("Failed to start model training pipeline", e);
        } finally {
            span.end();
        }
    }

    /**
     * Start ML Model Deployment Pipeline
     */
    public String startModelDeploymentPipeline(ModelDeploymentRequest request) {
        Span span = tracer.spanBuilder("ml.orchestrate.model-deployment")
            .setAttribute("model.id", request.getModelId())
            .setAttribute("environment", request.getTargetEnvironment())
            .startSpan();

        try {
            logger.info("Starting model deployment pipeline for model: {}", request.getModelId());

            MlModelDeploymentWorkflow workflow = workflowClient.newWorkflowStub(
                MlModelDeploymentWorkflow.class,
                WorkflowOptions.newBuilder()
                    .setTaskQueue("ml-pipeline-task-queue")
                    .setWorkflowId("model-deploy-" + request.getModelId())
                    .setWorkflowExecutionTimeout(Duration.ofHours(2))
                    .build()
            );

            WorkflowClient.start(workflow::deploy, request);

            span.setStatus(StatusCode.OK);
            logger.info("Model deployment pipeline started for model: {}", request.getModelId());

            return "model-deploy-" + request.getModelId();

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Failed to start model deployment pipeline for model: {}", request.getModelId(), e);
            throw new MlPipelineException("Failed to start model deployment pipeline", e);
        } finally {
            span.end();
        }
    }

    /**
     * Start Complete ML Pipeline (Feature Engineering → Training → Deployment)
     */
    public String startCompleteMlPipeline(CompleteMlPipelineRequest request) {
        Span span = tracer.spanBuilder("ml.orchestrate.complete-pipeline")
            .setAttribute("pipeline.id", request.getPipelineId())
            .setAttribute("model.type", request.getModelType())
            .startSpan();

        try {
            String pipelineId = request.getPipelineId();
            logger.info("Starting complete ML pipeline: {}", pipelineId);

            // Generate unique IDs for each step
            String featureEngId = UUID.randomUUID().toString();
            String trainingId = UUID.randomUUID().toString();
            String deploymentId = UUID.randomUUID().toString();

            // Start feature engineering
            var featureRequest = new FeatureEngineeringRequest(
                featureEngId,
                request.getDataSource(),
                request.getFeatureConfig()
            );
            startFeatureEngineeringPipeline(featureRequest);

            // Start model training (will wait for feature engineering completion via events)
            var trainingRequest = new ModelTrainingRequest(
                trainingId,
                featureEngId, // Depends on feature engineering
                request.getModelType(),
                request.getTrainingConfig()
            );
            startModelTrainingPipeline(trainingRequest);

            // Start deployment (will wait for training completion)
            var deploymentRequest = new ModelDeploymentRequest(
                deploymentId,
                trainingId, // Depends on training
                request.getTargetEnvironment(),
                request.getDeploymentConfig()
            );
            startModelDeploymentPipeline(deploymentRequest);

            span.setStatus(StatusCode.OK);
            logger.info("Complete ML pipeline started: {} with steps: {}, {}, {}",
                       pipelineId, featureEngId, trainingId, deploymentId);

            return pipelineId;

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Failed to start complete ML pipeline: {}", request.getPipelineId(), e);
            throw new MlPipelineException("Failed to start complete ML pipeline", e);
        } finally {
            span.end();
        }
    }

    /**
     * Cancel running pipeline
     */
    public void cancelPipeline(String workflowId) {
        try {
            WorkflowStub workflowStub = workflowClient.newUntypedWorkflowStub(workflowId);
            workflowStub.cancel();
            logger.info("Cancelled ML pipeline workflow: {}", workflowId);
        } catch (Exception e) {
            logger.error("Failed to cancel ML pipeline workflow: {}", workflowId, e);
            throw new MlPipelineException("Failed to cancel pipeline", e);
        }
    }

    /**
     * Get pipeline status
     */
    public PipelineStatus getPipelineStatus(String workflowId) {
        try {
            WorkflowStub workflowStub = workflowClient.newUntypedWorkflowStub(workflowId);
            var execution = workflowStub.getWorkflowExecution();

            return new PipelineStatus(
                workflowId,
                execution.getWorkflowId(),
                execution.getRunId(),
                workflowStub.getWorkflowExecution().getStatus().name(),
                null // Additional status details would come from workflow implementation
            );
        } catch (Exception e) {
            logger.error("Failed to get pipeline status for: {}", workflowId, e);
            return new PipelineStatus(workflowId, null, null, "UNKNOWN", e.getMessage());
        }
    }

    // Request/Response classes
    public static class FeatureEngineeringRequest {
        private final String pipelineId;
        private final String dataSource;
        private final java.util.Map<String, Object> featureConfig;

        public FeatureEngineeringRequest(String pipelineId, String dataSource,
                                       java.util.Map<String, Object> featureConfig) {
            this.pipelineId = pipelineId;
            this.dataSource = dataSource;
            this.featureConfig = featureConfig;
        }

        // Getters
        public String getPipelineId() { return pipelineId; }
        public String getDataSource() { return dataSource; }
        public java.util.Map<String, Object> getFeatureConfig() { return featureConfig; }
    }

    public static class ModelTrainingRequest {
        private final String pipelineId;
        private final String featurePipelineId; // Dependency
        private final String modelType;
        private final java.util.Map<String, Object> trainingConfig;

        public ModelTrainingRequest(String pipelineId, String featurePipelineId,
                                  String modelType, java.util.Map<String, Object> trainingConfig) {
            this.pipelineId = pipelineId;
            this.featurePipelineId = featurePipelineId;
            this.modelType = modelType;
            this.trainingConfig = trainingConfig;
        }

        // Getters
        public String getPipelineId() { return pipelineId; }
        public String getFeaturePipelineId() { return featurePipelineId; }
        public String getModelType() { return modelType; }
        public java.util.Map<String, Object> getTrainingConfig() { return trainingConfig; }
    }

    public static class ModelDeploymentRequest {
        private final String modelId;
        private final String trainingPipelineId; // Dependency
        private final String targetEnvironment;
        private final java.util.Map<String, Object> deploymentConfig;

        public ModelDeploymentRequest(String modelId, String trainingPipelineId,
                                    String targetEnvironment, java.util.Map<String, Object> deploymentConfig) {
            this.modelId = modelId;
            this.trainingPipelineId = trainingPipelineId;
            this.targetEnvironment = targetEnvironment;
            this.deploymentConfig = deploymentConfig;
        }

        // Getters
        public String getModelId() { return modelId; }
        public String getTrainingPipelineId() { return trainingPipelineId; }
        public String getTargetEnvironment() { return targetEnvironment; }
        public java.util.Map<String, Object> getDeploymentConfig() { return deploymentConfig; }
    }

    public static class CompleteMlPipelineRequest {
        private final String pipelineId;
        private final String dataSource;
        private final String modelType;
        private final String targetEnvironment;
        private final java.util.Map<String, Object> featureConfig;
        private final java.util.Map<String, Object> trainingConfig;
        private final java.util.Map<String, Object> deploymentConfig;

        public CompleteMlPipelineRequest(String pipelineId, String dataSource, String modelType,
                                       String targetEnvironment, java.util.Map<String, Object> featureConfig,
                                       java.util.Map<String, Object> trainingConfig,
                                       java.util.Map<String, Object> deploymentConfig) {
            this.pipelineId = pipelineId;
            this.dataSource = dataSource;
            this.modelType = modelType;
            this.targetEnvironment = targetEnvironment;
            this.featureConfig = featureConfig;
            this.trainingConfig = trainingConfig;
            this.deploymentConfig = deploymentConfig;
        }

        // Getters
        public String getPipelineId() { return pipelineId; }
        public String getDataSource() { return dataSource; }
        public String getModelType() { return modelType; }
        public String getTargetEnvironment() { return targetEnvironment; }
        public java.util.Map<String, Object> getFeatureConfig() { return featureConfig; }
        public java.util.Map<String, Object> getTrainingConfig() { return trainingConfig; }
        public java.util.Map<String, Object> getDeploymentConfig() { return deploymentConfig; }
    }

    public static class PipelineStatus {
        private final String workflowId;
        private final String executionId;
        private final String runId;
        private final String status;
        private final String details;

        public PipelineStatus(String workflowId, String executionId, String runId,
                            String status, String details) {
            this.workflowId = workflowId;
            this.executionId = executionId;
            this.runId = runId;
            this.status = status;
            this.details = details;
        }

        // Getters
        public String getWorkflowId() { return workflowId; }
        public String getExecutionId() { return executionId; }
        public String getRunId() { return runId; }
        public String getStatus() { return status; }
        public String getDetails() { return details; }
    }

    public static class MlPipelineException extends RuntimeException {
        public MlPipelineException(String message) {
            super(message);
        }

        public MlPipelineException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}