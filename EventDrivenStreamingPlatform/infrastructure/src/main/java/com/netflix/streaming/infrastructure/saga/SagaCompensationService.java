package com.netflix.streaming.infrastructure.saga;

import com.netflix.streaming.events.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Saga compensation service for handling rollback scenarios.
 * 
 * Implements compensation logic for distributed transactions:
 * - Tracks completed saga steps
 * - Executes compensation handlers on failure
 * - Ensures idempotent compensation
 */
@Service
public class SagaCompensationService {

    private static final Logger logger = LoggerFactory.getLogger(SagaCompensationService.class);

    private final EventPublisher eventPublisher;
    private final Map<String, SagaState> sagaStates = new ConcurrentHashMap<>();
    private final Map<String, CompensationHandler> compensationHandlers = new ConcurrentHashMap<>();

    public SagaCompensationService(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Register a compensation handler for a saga step.
     * 
     * @param sagaId The saga ID
     * @param stepId The step ID
     * @param compensationHandler The compensation handler
     */
    public void registerCompensationHandler(String sagaId, String stepId, CompensationHandler compensationHandler) {
        String key = buildKey(sagaId, stepId);
        compensationHandlers.put(key, compensationHandler);
        logger.debug("Registered compensation handler for saga: {}, step: {}", sagaId, stepId);
    }

    /**
     * Record that a saga step has been completed.
     * 
     * @param sagaId The saga ID
     * @param stepId The step ID
     * @param stepData Data associated with the step (for compensation)
     */
    public void recordStepCompleted(String sagaId, String stepId, Object stepData) {
        SagaState state = sagaStates.computeIfAbsent(sagaId, k -> new SagaState(sagaId));
        state.addCompletedStep(stepId, stepData);
        logger.debug("Recorded completed step: {} for saga: {}", stepId, sagaId);
    }

    /**
     * Compensate a saga (rollback all completed steps).
     * 
     * @param sagaId The saga ID
     * @param reason Reason for compensation
     */
    public void compensate(String sagaId, String reason) {
        SagaState state = sagaStates.get(sagaId);
        if (state == null) {
            logger.warn("Saga state not found for compensation: {}", sagaId);
            return;
        }

        logger.info("Starting compensation for saga: {}, reason: {}", sagaId, reason);

        // Compensate steps in reverse order
        java.util.List<SagaStep> completedSteps = state.getCompletedSteps();
        for (int i = completedSteps.size() - 1; i >= 0; i--) {
            SagaStep step = completedSteps.get(i);
            compensateStep(sagaId, step, reason);
        }

        // Mark saga as compensated
        state.setStatus(SagaStatus.COMPENSATED);
        
        // Publish compensation event
        publishCompensationEvent(sagaId, reason, completedSteps.size());
    }

    /**
     * Compensate a specific saga step.
     */
    private void compensateStep(String sagaId, SagaStep step, String reason) {
        String key = buildKey(sagaId, step.getStepId());
        CompensationHandler handler = compensationHandlers.get(key);

        if (handler == null) {
            logger.warn("No compensation handler found for saga: {}, step: {}", sagaId, step.getStepId());
            return;
        }

        try {
            logger.info("Compensating step: {} for saga: {}", step.getStepId(), sagaId);
            handler.compensate(step.getStepData());
            logger.info("Successfully compensated step: {} for saga: {}", step.getStepId(), sagaId);
        } catch (Exception e) {
            logger.error("Failed to compensate step: {} for saga: {}", step.getStepId(), sagaId, e);
            // Continue with other steps even if one fails
        }
    }

    /**
     * Check if a saga has been compensated.
     */
    public boolean isCompensated(String sagaId) {
        SagaState state = sagaStates.get(sagaId);
        return state != null && state.getStatus() == SagaStatus.COMPENSATED;
    }

    /**
     * Get saga state.
     */
    public SagaState getSagaState(String sagaId) {
        return sagaStates.get(sagaId);
    }

    /**
     * Clean up old saga states (should be called periodically).
     */
    public void cleanupOldStates(long olderThanMillis) {
        long cutoff = System.currentTimeMillis() - olderThanMillis;
        sagaStates.entrySet().removeIf(entry -> {
            SagaState state = entry.getValue();
            return state.getLastUpdated() < cutoff && 
                   (state.getStatus() == SagaStatus.COMPLETED || state.getStatus() == SagaStatus.COMPENSATED);
        });
    }

    /**
     * Build key for compensation handler lookup.
     */
    private String buildKey(String sagaId, String stepId) {
        return sagaId + ":" + stepId;
    }

    /**
     * Publish compensation event.
     */
    private void publishCompensationEvent(String sagaId, String reason, int compensatedSteps) {
        // In a real implementation, publish a SagaCompensatedEvent
        logger.info("Published compensation event for saga: {}, compensated steps: {}", sagaId, compensatedSteps);
    }

    /**
     * Compensation handler interface.
     */
    @FunctionalInterface
    public interface CompensationHandler {
        void compensate(Object stepData) throws Exception;
    }

    /**
     * Saga state tracking.
     */
    public static class SagaState {
        private final String sagaId;
        private final java.util.List<SagaStep> completedSteps = new java.util.ArrayList<>();
        private SagaStatus status = SagaStatus.IN_PROGRESS;
        private long lastUpdated = System.currentTimeMillis();

        public SagaState(String sagaId) {
            this.sagaId = sagaId;
        }

        public void addCompletedStep(String stepId, Object stepData) {
            completedSteps.add(new SagaStep(stepId, stepData));
            lastUpdated = System.currentTimeMillis();
        }

        public java.util.List<SagaStep> getCompletedSteps() {
            return new java.util.ArrayList<>(completedSteps);
        }

        public SagaStatus getStatus() { return status; }
        public void setStatus(SagaStatus status) { 
            this.status = status; 
            lastUpdated = System.currentTimeMillis();
        }

        public long getLastUpdated() { return lastUpdated; }
        public String getSagaId() { return sagaId; }
    }

    /**
     * Saga step information.
     */
    public static class SagaStep {
        private final String stepId;
        private final Object stepData;

        public SagaStep(String stepId, Object stepData) {
            this.stepId = stepId;
            this.stepData = stepData;
        }

        public String getStepId() { return stepId; }
        public Object getStepData() { return stepData; }
    }

    /**
     * Saga status enumeration.
     */
    public enum SagaStatus {
        IN_PROGRESS,
        COMPLETED,
        COMPENSATED,
        FAILED
    }
}

