package com.example.kotlinpay.risk.service

import com.example.kotlinpay.risk.domain.*
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class RiskEvaluationService(
    private val rules: List<RiskRule>,
    private val meterRegistry: MeterRegistry
) {

    private val logger = LoggerFactory.getLogger(RiskEvaluationService::class.java)

    // Monitoring metrics
    private val evaluationTimer = Timer.builder("risk.evaluation.duration")
        .description("Time taken to evaluate payment risk")
        .register(meterRegistry)

    private val evaluationCounter = Counter.builder("risk.evaluation.total")
        .description("Total number of risk evaluations")
        .register(meterRegistry)

    private val highRiskCounter = Counter.builder("risk.evaluation.high_risk")
        .description("High-risk evaluations detected")
        .register(meterRegistry)

    private val blockedTransactionCounter = Counter.builder("risk.evaluation.blocked")
        .description("Transactions blocked by risk evaluation")
        .register(meterRegistry)

    /**
     * Evaluate payment risk using all configured rules
     */
    fun evaluateRisk(request: RiskEvaluationRequest): RiskEvaluationResult {
        val requestId = java.util.UUID.randomUUID().toString()
        logger.info("Starting risk evaluation [requestId={}, paymentId={}]", requestId, request.paymentId)

        return evaluationTimer.recordCallable {
            try {
                evaluationCounter.increment()

                val ruleEvaluations = rules.map { rule ->
                    logger.debug("Evaluating rule: {} [requestId={}]", rule.name, requestId)
                    rule.evaluate(request)
                }

                val totalScore = ruleEvaluations.sumOf { it.score }
                val allReasons = ruleEvaluations.flatMap { it.reasons }

                val riskLevel = calculateRiskLevel(totalScore)
                val decision = calculateDecision(totalScore, riskLevel)

                // Monitoring
                if (riskLevel == RiskLevel.HIGH || riskLevel == RiskLevel.CRITICAL) {
                    highRiskCounter.increment()
                }

                if (decision == RiskDecision.DECLINE || decision == RiskDecision.QUARANTINE) {
                    blockedTransactionCounter.increment()
                }

                val result = RiskEvaluationResult(
                    paymentId = request.paymentId,
                    riskScore = totalScore.coerceAtMost(100),
                    riskLevel = riskLevel,
                    decision = decision,
                    reasons = allReasons,
                    evaluatedAt = LocalDateTime.now(),
                    evaluatedBy = "RiskEvaluationService"
                )

                logger.info("Risk evaluation completed [requestId={}, paymentId={}, score={}, decision={}]",
                    requestId, request.paymentId, totalScore, decision)

                result

            } catch (e: Exception) {
                logger.error("Risk evaluation failed [requestId={}, paymentId={}]", requestId, request.paymentId, e)
                // Return high-risk result on evaluation failure (fail-safe)
                RiskEvaluationResult(
                    paymentId = request.paymentId,
                    riskScore = 100,
                    riskLevel = RiskLevel.CRITICAL,
                    decision = RiskDecision.QUARANTINE,
                    reasons = listOf("Risk evaluation failed: ${e.message}"),
                    evaluatedAt = LocalDateTime.now(),
                    evaluatedBy = "RiskEvaluationService"
                )
            }
        }
    }

    private fun calculateRiskLevel(score: Int): RiskLevel {
        return when (score) {
            in 0..25 -> RiskLevel.LOW
            in 26..50 -> RiskLevel.MEDIUM
            in 51..75 -> RiskLevel.HIGH
            else -> RiskLevel.CRITICAL
        }
    }

    private fun calculateDecision(score: Int, riskLevel: RiskLevel): RiskDecision {
        return when {
            score >= 80 -> RiskDecision.DECLINE
            riskLevel == RiskLevel.CRITICAL -> RiskDecision.QUARANTINE
            riskLevel == RiskLevel.HIGH -> RiskDecision.REVIEW
            else -> RiskDecision.APPROVE
        }
    }
}
