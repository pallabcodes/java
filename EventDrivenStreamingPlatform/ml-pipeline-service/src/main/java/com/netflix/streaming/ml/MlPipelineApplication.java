package com.netflix.streaming.ml;

import com.netflix.streaming.ml.orchestrator.MlPipelineOrchestrator;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * ML Pipeline Service Application.
 *
 * This service demonstrates:
 * - Saga Pattern: Distributed transaction orchestration for ML workflows
 * - Event-Driven Coordination: Saga steps communicate via domain events
 * - Compensation Logic: Rollback failed ML pipeline steps
 * - Feature Engineering: Multi-step ML data processing pipelines
 * - Model Training Orchestration: Coordinated model lifecycle management
 *
 * Architecture:
 * Events → Saga Orchestrator → Feature Engineering → Model Training → Deployment
 *    ↓         ↓                        ↓               ↓             ↓
 * Kafka → Temporal Workflows → Data Processing → ML Training → Model Serving
 */
@SpringBootApplication
@EnableKafka
public class MlPipelineApplication {

    private static final Logger logger = LoggerFactory.getLogger(MlPipelineApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Netflix ML Pipeline Service...");
        SpringApplication.run(MlPipelineApplication.class, args);
        logger.info("ML Pipeline Service started successfully!");
    }

    /**
     * OpenTelemetry Tracer for distributed tracing
     */
    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer("ml-pipeline-service", "1.0.0");
    }

    /**
     * OpenTelemetry TextMapPropagator for context propagation
     */
    @Bean
    public TextMapPropagator textMapPropagator(OpenTelemetry openTelemetry) {
        return openTelemetry.getPropagators().getTextMapPropagator();
    }

    /**
     * Temporal Workflow Service Stubs
     */
    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        return WorkflowServiceStubs.newLocalServiceStubs();
    }

    /**
     * Temporal Workflow Client
     */
    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs serviceStubs) {
        return WorkflowClient.newInstance(serviceStubs);
    }

    /**
     * Temporal Worker Factory for saga orchestration
     */
    @Bean
    public WorkerFactory workerFactory(WorkflowClient workflowClient) {
        WorkerFactory factory = WorkerFactory.newInstance(workflowClient);

        // Create worker for ML pipeline workflows
        Worker worker = factory.newWorker("ml-pipeline-task-queue");

        // Register workflow and activity implementations
        worker.registerWorkflowImplementationTypes(
            MlFeatureEngineeringWorkflowImpl.class,
            MlModelTrainingWorkflowImpl.class,
            MlModelDeploymentWorkflowImpl.class
        );

        worker.registerActivitiesImplementations(
            new FeatureEngineeringActivities(),
            new ModelTrainingActivities(),
            new ModelDeploymentActivities()
        );

        factory.start();
        logger.info("Temporal workers started for ML pipeline orchestration");

        return factory;
    }

    /**
     * ML Pipeline Orchestrator
     */
    @Bean
    public MlPipelineOrchestrator mlPipelineOrchestrator(WorkflowClient workflowClient) {
        return new MlPipelineOrchestrator(workflowClient);
    }
}

// Configuration for ML pipeline components
@Configuration
class MlPipelineConfig {

    @Bean
    public FeatureEngineeringService featureEngineeringService() {
        return new FeatureEngineeringService();
    }

    @Bean
    public ModelTrainingService modelTrainingService() {
        return new ModelTrainingService();
    }

    @Bean
    public ModelDeploymentService modelDeploymentService() {
        return new ModelDeploymentService();
    }

    @Bean
    public MlPipelineEventProcessor mlPipelineEventProcessor(
            MlPipelineOrchestrator orchestrator) {
        return new MlPipelineEventProcessor(orchestrator);
    }
}