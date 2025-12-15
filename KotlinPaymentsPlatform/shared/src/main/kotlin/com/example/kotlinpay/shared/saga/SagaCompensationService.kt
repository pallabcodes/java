package com.example.kotlinpay.shared.saga

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

/**
 * Saga compensation service for handling rollback scenarios.
 */
@Service
class SagaCompensationService {

    private val logger = LoggerFactory.getLogger(SagaCompensationService::class.java)
    private val sagaStates = ConcurrentHashMap<String, SagaState>()
    private val compensationHandlers = ConcurrentHashMap<String, CompensationHandler>()

    /**
     * Register a compensation handler for a saga step.
     */
    fun registerCompensationHandler(
        sagaId: String,
        stepId: String,
        compensationHandler: CompensationHandler
    ) {
        val key = buildKey(sagaId, stepId)
        compensationHandlers[key] = compensationHandler
        logger.debug("Registered compensation handler for saga: {}, step: {}", sagaId, stepId)
    }

    /**
     * Record that a saga step has been completed.
     */
    fun recordStepCompleted(sagaId: String, stepId: String, stepData: Any?) {
        val state = sagaStates.computeIfAbsent(sagaId) { SagaState(sagaId) }
        state.addCompletedStep(stepId, stepData)
        logger.debug("Recorded completed step: {} for saga: {}", stepId, sagaId)
    }

    /**
     * Compensate a saga (rollback all completed steps).
     */
    fun compensate(sagaId: String, reason: String) {
        val state = sagaStates[sagaId] ?: run {
            logger.warn("Saga state not found for compensation: {}", sagaId)
            return
        }

        logger.info("Starting compensation for saga: {}, reason: {}", sagaId, reason)

        val completedSteps = state.getCompletedSteps()
        for (i in completedSteps.size - 1 downTo 0) {
            val step = completedSteps[i]
            compensateStep(sagaId, step, reason)
        }

        state.status = SagaStatus.COMPENSATED
        publishCompensationEvent(sagaId, reason, completedSteps.size)
    }

    private fun compensateStep(sagaId: String, step: SagaStep, reason: String) {
        val key = buildKey(sagaId, step.stepId)
        val handler = compensationHandlers[key] ?: run {
            logger.warn("No compensation handler found for saga: {}, step: {}", sagaId, step.stepId)
            return
        }

        try {
            logger.info("Compensating step: {} for saga: {}", step.stepId, sagaId)
            handler.compensate(step.stepData)
            logger.info("Successfully compensated step: {} for saga: {}", step.stepId, sagaId)
        } catch (e: Exception) {
            logger.error("Failed to compensate step: {} for saga: {}", step.stepId, sagaId, e)
        }
    }

    fun isCompensated(sagaId: String): Boolean {
        return sagaStates[sagaId]?.status == SagaStatus.COMPENSATED
    }

    fun getSagaState(sagaId: String): SagaState? {
        return sagaStates[sagaId]
    }

    fun cleanupOldStates(olderThanMillis: Long) {
        val cutoff = System.currentTimeMillis() - olderThanMillis
        sagaStates.entries.removeIf { (_, state) ->
            state.lastUpdated < cutoff && 
            (state.status == SagaStatus.COMPLETED || state.status == SagaStatus.COMPENSATED)
        }
    }

    private fun buildKey(sagaId: String, stepId: String): String {
        return "$sagaId:$stepId"
    }

    private fun publishCompensationEvent(sagaId: String, reason: String, compensatedSteps: Int) {
        logger.info("Published compensation event for saga: {}, compensated steps: {}", sagaId, compensatedSteps)
    }

    /**
     * Compensation handler interface.
     */
    fun interface CompensationHandler {
        fun compensate(stepData: Any?)
    }

    /**
     * Saga state tracking.
     */
    class SagaState(val sagaId: String) {
        private val completedSteps = mutableListOf<SagaStep>()
        var status: SagaStatus = SagaStatus.IN_PROGRESS
            set(value) {
                field = value
                lastUpdated = System.currentTimeMillis()
            }
        var lastUpdated: Long = System.currentTimeMillis()
            private set

        fun addCompletedStep(stepId: String, stepData: Any?) {
            completedSteps.add(SagaStep(stepId, stepData))
            lastUpdated = System.currentTimeMillis()
        }

        fun getCompletedSteps(): List<SagaStep> {
            return completedSteps.toList()
        }
    }

    /**
     * Saga step information.
     */
    data class SagaStep(
        val stepId: String,
        val stepData: Any?
    )

    /**
     * Saga status enumeration.
     */
    enum class SagaStatus {
        IN_PROGRESS,
        COMPLETED,
        COMPENSATED,
        FAILED
    }
}

