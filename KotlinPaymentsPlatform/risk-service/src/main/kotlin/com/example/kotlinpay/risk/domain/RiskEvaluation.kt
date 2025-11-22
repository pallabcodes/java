package com.example.kotlinpay.risk.domain

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Risk evaluation request
 */
data class RiskEvaluationRequest(
    val paymentId: String,
    val amount: BigDecimal,
    val currency: String,
    val customerId: String,
    val merchantId: String,
    val cardLastFour: String,
    val countryCode: String,
    val ipAddress: String,
    val userAgent: String
)

/**
 * Risk evaluation result
 */
data class RiskEvaluationResult(
    val paymentId: String,
    val riskScore: Int, // 0-100, higher = riskier
    val riskLevel: RiskLevel,
    val decision: RiskDecision,
    val reasons: List<String>,
    val evaluatedAt: LocalDateTime,
    val evaluatedBy: String
)

/**
 * Risk level enumeration
 */
enum class RiskLevel {
    LOW,      // 0-25
    MEDIUM,   // 26-50
    HIGH,     // 51-75
    CRITICAL  // 76-100
}

/**
 * Risk decision enumeration
 */
enum class RiskDecision {
    APPROVE,      // Allow the payment
    REVIEW,       // Manual review required
    DECLINE,      // Block the payment
    QUARANTINE    // Hold for further investigation
}

/**
 * Risk rule interface
 */
interface RiskRule {
    val name: String
    val weight: Int // Contribution to total risk score

    fun evaluate(request: RiskEvaluationRequest): RiskEvaluation
}

data class RiskEvaluation(
    val triggered: Boolean,
    val score: Int,
    val reasons: List<String>
)
