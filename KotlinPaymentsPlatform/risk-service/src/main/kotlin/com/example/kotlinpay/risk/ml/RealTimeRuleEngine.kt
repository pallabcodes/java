package com.example.kotlinpay.risk.ml

import com.example.kotlinpay.shared.compliance.AuditLogger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

/**
 * Real-Time Rule Engine for Fraud Detection
 *
 * Implements rule-based fraud detection with dynamic rule updates:
 * - Velocity rules (transaction frequency limits)
 * - Amount-based rules (unusual transaction sizes)
 * - Geographic rules (location-based fraud detection)
 * - Time-based rules (unusual transaction timing)
 * - Behavioral rules (deviation from normal patterns)
 * - Dynamic rule updates based on emerging threats
 * - Rule performance monitoring and optimization
 */
@Service
class RealTimeRuleEngine(
    private val auditLogger: AuditLogger
) {
    private val logger = LoggerFactory.getLogger(RealTimeRuleEngine::class.java)

    // Active rules registry
    private val activeRules = ConcurrentHashMap<String, FraudRule>()

    // Rule execution statistics
    private val ruleStats = ConcurrentHashMap<String, RuleStats>()

    // User behavior baselines
    private val userBaselines = ConcurrentHashMap<String, UserBaseline>()

    // Dynamic rule updates
    private val ruleUpdates = ConcurrentHashMap<String, RuleUpdate>()

    init {
        initializeDefaultRules()
    }

    /**
     * Evaluate transaction against all active rules
     */
    fun evaluateRules(transaction: TransactionData): RuleEvaluation {
        val startTime = System.nanoTime()

        val triggeredRules = mutableListOf<String>()
        var totalScore = 0.0

        // Evaluate each active rule
        activeRules.values.forEach { rule ->
            try {
                val result = evaluateRule(rule, transaction)
                if (result.triggered) {
                    triggeredRules.add(rule.name)
                    totalScore += result.score

                    // Update rule statistics
                    updateRuleStats(rule.name, true, result.score)

                    // Log rule trigger
                    auditLogger.logSecurityEvent(
                        event = "RULE_TRIGGERED",
                        severity = if (result.score > 0.7) "HIGH" else "MEDIUM",
                        details = mapOf(
                            "rule_name" to rule.name,
                            "transaction_id" to transaction.id,
                            "score" to result.score.toString(),
                            "user_id" to transaction.userId
                        )
                    )
                } else {
                    updateRuleStats(rule.name, false, 0.0)
                }
            } catch (e: Exception) {
                logger.error("Rule evaluation failed for rule: ${rule.name}", e)
                updateRuleStats(rule.name, false, 0.0, error = true)
            }
        }

        // Normalize score to 0-1 range
        val normalizedScore = totalScore.coerceIn(0.0, 1.0)

        val evaluationTime = (System.nanoTime() - startTime) / 1_000_000 // Convert to milliseconds

        // Update user baseline with this transaction
        updateUserBaseline(transaction)

        return RuleEvaluation(
            score = normalizedScore,
            triggeredRules = triggeredRules,
            evaluationTimeMs = evaluationTime.toDouble(),
            totalRules = activeRules.size
        )
    }

    /**
     * Add or update a fraud detection rule
     */
    fun addRule(rule: FraudRule): Boolean {
        return try {
            activeRules[rule.name] = rule
            ruleStats[rule.name] = RuleStats(rule.name)

            auditLogger.logSecurityEvent(
                event = "RULE_ADDED",
                severity = "INFO",
                details = mapOf(
                    "rule_name" to rule.name,
                    "rule_type" to rule.type.name,
                    "severity" to rule.severity.name
                )
            )

            logger.info("Added fraud detection rule: ${rule.name}")
            true
        } catch (e: Exception) {
            logger.error("Failed to add rule: ${rule.name}", e)
            false
        }
    }

    /**
     * Remove a fraud detection rule
     */
    fun removeRule(ruleName: String): Boolean {
        return try {
            val removed = activeRules.remove(ruleName) != null
            if (removed) {
                ruleStats.remove(ruleName)

                auditLogger.logSecurityEvent(
                    event = "RULE_REMOVED",
                    severity = "WARNING",
                    details = mapOf("rule_name" to ruleName)
                )

                logger.info("Removed fraud detection rule: $ruleName")
            }
            removed
        } catch (e: Exception) {
            logger.error("Failed to remove rule: $ruleName", e)
            false
        }
    }

    /**
     * Update rule parameters dynamically
     */
    fun updateRuleParameters(ruleName: String, parameters: Map<String, Any>): Boolean {
        return try {
            val rule = activeRules[ruleName]
            if (rule != null) {
                val updatedRule = rule.copy(parameters = rule.parameters + parameters)
                activeRules[ruleName] = updatedRule

                auditLogger.logSecurityEvent(
                    event = "RULE_UPDATED",
                    severity = "INFO",
                    details = mapOf(
                        "rule_name" to ruleName,
                        "updated_parameters" to parameters.keys.joinToString(",")
                    )
                )

                logger.info("Updated rule parameters for: $ruleName")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            logger.error("Failed to update rule parameters for: $ruleName", e)
            false
        }
    }

    /**
     * Get rule performance statistics
     */
    fun getRuleStats(ruleName: String): RuleStats? {
        return ruleStats[ruleName]
    }

    /**
     * Get all rule statistics
     */
    fun getAllRuleStats(): Map<String, RuleStats> {
        return ruleStats.toMap()
    }

    /**
     * Create dynamic rule based on emerging fraud patterns
     */
    fun createDynamicRule(pattern: FraudPattern, name: String): FraudRule? {
        return try {
            val rule = when (pattern.type) {
                PatternType.VELOCITY_SPIKE -> createVelocityRule(pattern, name)
                PatternType.AMOUNT_ANOMALY -> createAmountRule(pattern, name)
                PatternType.GEOGRAPHIC_SHIFT -> createGeographicRule(pattern, name)
                PatternType.TIME_ANOMALY -> createTimeRule(pattern, name)
            }

            if (rule != null) {
                addRule(rule)

                auditLogger.logSecurityEvent(
                    event = "DYNAMIC_RULE_CREATED",
                    severity = "INFO",
                    details = mapOf(
                        "rule_name" to name,
                        "pattern_type" to pattern.type.name,
                        "triggered_by" to pattern.detectedBy
                    )
                )
            }

            rule
        } catch (e: Exception) {
            logger.error("Failed to create dynamic rule: $name", e)
            null
        }
    }

    /**
     * Update user behavior baseline
     */
    private fun updateUserBaseline(transaction: TransactionData) {
        val baseline = userBaselines.getOrPut(transaction.userId) {
            UserBaseline(
                userId = transaction.userId,
                transactionCount = 0,
                totalAmount = 0.0,
                averageAmount = 0.0,
                lastTransactionTime = LocalDateTime.now().minusDays(1),
                usualCities = mutableSetOf(),
                usualMerchants = mutableSetOf(),
                velocityPatterns = mutableListOf()
            )
        }

        // Update baseline statistics
        baseline.transactionCount++
        baseline.totalAmount += transaction.amount
        baseline.averageAmount = baseline.totalAmount / baseline.transactionCount
        baseline.lastTransactionTime = transaction.timestamp
        baseline.usualCities.add(transaction.city)
        baseline.usualMerchants.add(transaction.merchantId)

        // Update velocity patterns (transactions per hour)
        val recentTransactions = baseline.velocityPatterns.takeLast(10)
        recentTransactions + transaction.timestamp

        baseline.velocityPatterns = (recentTransactions + transaction.timestamp).toMutableList()
    }

    /**
     * Evaluate individual rule against transaction
     */
    private fun evaluateRule(rule: FraudRule, transaction: TransactionData): RuleResult {
        return when (rule.type) {
            RuleType.VELOCITY -> evaluateVelocityRule(rule, transaction)
            RuleType.AMOUNT -> evaluateAmountRule(rule, transaction)
            RuleType.GEOGRAPHIC -> evaluateGeographicRule(rule, transaction)
            RuleType.TIME -> evaluateTimeRule(rule, transaction)
            RuleType.BEHAVIORAL -> evaluateBehavioralRule(rule, transaction)
            RuleType.MERCHANT -> evaluateMerchantRule(rule, transaction)
            RuleType.PAYMENT_METHOD -> evaluatePaymentMethodRule(rule, transaction)
        }
    }

    /**
     * Velocity-based rule evaluation
     */
    private fun evaluateVelocityRule(rule: FraudRule, transaction: TransactionData): RuleResult {
        val baseline = userBaselines[transaction.userId] ?: return RuleResult(false, 0.0)

        // Calculate transaction velocity (transactions per hour)
        val recentTransactions = baseline.velocityPatterns
        val oneHourAgo = transaction.timestamp.minusHours(1)
        val recentCount = recentTransactions.count { it.isAfter(oneHourAgo) }

        val threshold = rule.parameters["max_transactions_per_hour"] as? Double ?: 5.0
        val triggered = recentCount >= threshold

        val score = if (triggered) {
            minOf((recentCount - threshold + 1) / threshold, 1.0) * rule.severity.multiplier
        } else {
            0.0
        }

        return RuleResult(triggered, score)
    }

    /**
     * Amount-based rule evaluation
     */
    private fun evaluateAmountRule(rule: FraudRule, transaction: TransactionData): RuleResult {
        val baseline = userBaselines[transaction.userId] ?: return RuleResult(false, 0.0)

        val amountDeviation = abs(transaction.amount - baseline.averageAmount) / baseline.averageAmount
        val threshold = rule.parameters["max_deviation_percent"] as? Double ?: 2.0

        val triggered = amountDeviation > threshold
        val score = if (triggered) {
            minOf(amountDeviation / threshold, 2.0) * rule.severity.multiplier
        } else {
            0.0
        }

        return RuleResult(triggered, score)
    }

    /**
     * Geographic rule evaluation
     */
    private fun evaluateGeographicRule(rule: FraudRule, transaction: TransactionData): RuleResult {
        val baseline = userBaselines[transaction.userId] ?: return RuleResult(false, 0.0)

        val isUsualCity = baseline.usualCities.contains(transaction.city)
        val isUsualCountry = transaction.country == "US" // Assuming US is usual

        val triggered = !isUsualCity && !isUsualCountry
        val score = if (triggered) rule.severity.multiplier else 0.0

        return RuleResult(triggered, score)
    }

    /**
     * Time-based rule evaluation
     */
    private fun evaluateTimeRule(rule: FraudRule, transaction: TransactionData): RuleResult {
        val hour = transaction.timestamp.hour
        val unusualHours = rule.parameters["unusual_hours"] as? List<Int> ?: listOf(2, 3, 4, 5, 6)

        val triggered = unusualHours.contains(hour)
        val score = if (triggered) rule.severity.multiplier else 0.0

        return RuleResult(triggered, score)
    }

    /**
     * Behavioral rule evaluation
     */
    private fun evaluateBehavioralRule(rule: FraudRule, transaction: TransactionData): RuleResult {
        val baseline = userBaselines[transaction.userId] ?: return RuleResult(false, 0.0)

        // Check for sudden behavior changes
        val recentVelocity = calculateRecentVelocity(baseline.velocityPatterns, transaction.timestamp)
        val normalVelocity = baseline.velocityPatterns.size / 24.0 // Average per hour over last 24h

        val velocitySpike = recentVelocity > normalVelocity * 3
        val triggered = velocitySpike

        val score = if (triggered) {
            minOf(recentVelocity / normalVelocity, 3.0) * rule.severity.multiplier
        } else {
            0.0
        }

        return RuleResult(triggered, score)
    }

    /**
     * Merchant-based rule evaluation
     */
    private fun evaluateMerchantRule(rule: FraudRule, transaction: TransactionData): RuleResult {
        val baseline = userBaselines[transaction.userId] ?: return RuleResult(false, 0.0)

        val isUsualMerchant = baseline.usualMerchants.contains(transaction.merchantId)
        val highRiskMerchants = rule.parameters["high_risk_merchants"] as? List<String> ?: emptyList()

        val isHighRisk = highRiskMerchants.contains(transaction.merchantId)
        val triggered = !isUsualMerchant || isHighRisk

        val score = when {
            isHighRisk -> rule.severity.multiplier
            !isUsualMerchant -> rule.severity.multiplier * 0.5
            else -> 0.0
        }

        return RuleResult(triggered, score)
    }

    /**
     * Payment method rule evaluation
     */
    private fun evaluatePaymentMethodRule(rule: FraudRule, transaction: TransactionData): RuleResult {
        val suspiciousMethods = rule.parameters["suspicious_methods"] as? List<String> ?: listOf("unknown")
        val triggered = suspiciousMethods.contains(transaction.paymentMethod)

        val score = if (triggered) rule.severity.multiplier else 0.0

        return RuleResult(triggered, score)
    }

    /**
     * Initialize default fraud detection rules
     */
    private fun initializeDefaultRules() {
        val rules = listOf(
            // Velocity rules
            FraudRule(
                name = "high_velocity_transactions",
                type = RuleType.VELOCITY,
                severity = RuleSeverity.HIGH,
                parameters = mapOf("max_transactions_per_hour" to 10.0)
            ),

            FraudRule(
                name = "suspicious_velocity_spike",
                type = RuleType.VELOCITY,
                severity = RuleSeverity.MEDIUM,
                parameters = mapOf("max_transactions_per_hour" to 5.0)
            ),

            // Amount rules
            FraudRule(
                name = "unusual_amount",
                type = RuleType.AMOUNT,
                severity = RuleSeverity.HIGH,
                parameters = mapOf("max_deviation_percent" to 3.0)
            ),

            FraudRule(
                name = "large_amount",
                type = RuleType.AMOUNT,
                severity = RuleSeverity.MEDIUM,
                parameters = mapOf("min_amount" to 1000.0)
            ),

            // Geographic rules
            FraudRule(
                name = "unusual_location",
                type = RuleType.GEOGRAPHIC,
                severity = RuleSeverity.HIGH,
                parameters = emptyMap()
            ),

            FraudRule(
                name = "international_transaction",
                type = RuleType.GEOGRAPHIC,
                severity = RuleSeverity.MEDIUM,
                parameters = mapOf("high_risk_countries" to listOf("NG", "RU", "CN"))
            ),

            // Time rules
            FraudRule(
                name = "unusual_hours",
                type = RuleType.TIME,
                severity = RuleSeverity.MEDIUM,
                parameters = mapOf("unusual_hours" to listOf(1, 2, 3, 4, 5, 6))
            ),

            // Behavioral rules
            FraudRule(
                name = "behavioral_anomaly",
                type = RuleType.BEHAVIORAL,
                severity = RuleSeverity.HIGH,
                parameters = emptyMap()
            ),

            // Merchant rules
            FraudRule(
                name = "unusual_merchant",
                type = RuleType.MERCHANT,
                severity = RuleSeverity.MEDIUM,
                parameters = emptyMap()
            ),

            FraudRule(
                name = "high_risk_merchant",
                type = RuleType.MERCHANT,
                severity = RuleSeverity.HIGH,
                parameters = mapOf("high_risk_merchants" to listOf("crypto_exchange", "gambling_site"))
            )
        )

        rules.forEach { addRule(it) }
    }

    /**
     * Helper methods for dynamic rule creation
     */

    private fun createVelocityRule(pattern: FraudPattern, name: String): FraudRule {
        val threshold = pattern.parameters["detected_velocity"] as? Double ?: 10.0
        return FraudRule(
            name = name,
            type = RuleType.VELOCITY,
            severity = RuleSeverity.HIGH,
            parameters = mapOf("max_transactions_per_hour" to threshold * 0.8) // Slightly lower threshold
        )
    }

    private fun createAmountRule(pattern: FraudPattern, name: String): FraudRule {
        val threshold = pattern.parameters["detected_deviation"] as? Double ?: 2.0
        return FraudRule(
            name = name,
            type = RuleType.AMOUNT,
            severity = RuleSeverity.MEDIUM,
            parameters = mapOf("max_deviation_percent" to threshold * 0.9)
        )
    }

    private fun createGeographicRule(pattern: FraudPattern, name: String): FraudRule {
        return FraudRule(
            name = name,
            type = RuleType.GEOGRAPHIC,
            severity = RuleSeverity.HIGH,
            parameters = mapOf("detected_countries" to pattern.parameters["countries"])
        )
    }

    private fun createTimeRule(pattern: FraudPattern, name: String): FraudRule {
        val detectedHours = pattern.parameters["detected_hours"] as? List<Int> ?: emptyList()
        return FraudRule(
            name = name,
            type = RuleType.TIME,
            severity = RuleSeverity.MEDIUM,
            parameters = mapOf("unusual_hours" to detectedHours)
        )
    }

    /**
     * Utility methods
     */

    private fun updateRuleStats(ruleName: String, triggered: Boolean, score: Double, error: Boolean = false) {
        val stats = ruleStats.getOrPut(ruleName) { RuleStats(ruleName) }

        stats.totalEvaluations++
        if (triggered) {
            stats.triggerCount++
            stats.totalScore += score
            stats.averageScore = stats.totalScore / stats.triggerCount
        }
        if (error) {
            stats.errorCount++
        }

        stats.lastEvaluated = LocalDateTime.now()
    }

    private fun calculateRecentVelocity(transactions: List<LocalDateTime>, currentTime: LocalDateTime): Double {
        val oneHourAgo = currentTime.minusHours(1)
        val recentCount = transactions.count { it.isAfter(oneHourAgo) }
        return recentCount.toDouble()
    }

    /**
     * Get rule engine statistics
     */
    fun getRuleEngineStats(): RuleEngineStats {
        val now = LocalDateTime.now()
        val last24Hours = now.minusHours(24)

        val recentEvaluations = ruleStats.values.sumOf { stats ->
            if (stats.lastEvaluated?.isAfter(last24Hours) == true) stats.totalEvaluations else 0
        }

        val mostTriggeredRule = ruleStats.values.maxByOrNull { it.triggerCount }
        val averageRuleScore = ruleStats.values.map { it.averageScore }.average()

        return RuleEngineStats(
            totalRules = activeRules.size,
            evaluationsLast24Hours = recentEvaluations,
            mostTriggeredRule = mostTriggeredRule?.ruleName,
            averageRuleScore = averageRuleScore,
            ruleErrorRate = ruleStats.values.sumOf { it.errorCount }.toDouble() / ruleStats.values.sumOf { it.totalEvaluations }
        )
    }
}

/**
 * Data classes for rule engine
 */

data class FraudRule(
    val name: String,
    val type: RuleType,
    val severity: RuleSeverity,
    val parameters: Map<String, Any>,
    val enabled: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class RuleResult(
    val triggered: Boolean,
    val score: Double
)

data class RuleEvaluation(
    val score: Double,
    val triggeredRules: List<String>,
    val evaluationTimeMs: Double = 0.0,
    val totalRules: Int = 0
)

data class RuleStats(
    val ruleName: String,
    var totalEvaluations: Int = 0,
    var triggerCount: Int = 0,
    var totalScore: Double = 0.0,
    var averageScore: Double = 0.0,
    var errorCount: Int = 0,
    var lastEvaluated: LocalDateTime? = null
)

data class UserBaseline(
    val userId: String,
    var transactionCount: Int,
    var totalAmount: Double,
    var averageAmount: Double,
    var lastTransactionTime: LocalDateTime,
    val usualCities: MutableSet<String>,
    val usualMerchants: MutableSet<String>,
    var velocityPatterns: MutableList<LocalDateTime>
)

data class FraudPattern(
    val type: PatternType,
    val detectedBy: String,
    val parameters: Map<String, Any>,
    val confidence: Double,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class RuleUpdate(
    val ruleName: String,
    val updateType: UpdateType,
    val parameters: Map<String, Any>,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class RuleEngineStats(
    val totalRules: Int,
    val evaluationsLast24Hours: Int,
    val mostTriggeredRule: String?,
    val averageRuleScore: Double,
    val ruleErrorRate: Double
)

enum class RuleType {
    VELOCITY,
    AMOUNT,
    GEOGRAPHIC,
    TIME,
    BEHAVIORAL,
    MERCHANT,
    PAYMENT_METHOD
}

enum class RuleSeverity(val multiplier: Double) {
    LOW(0.3),
    MEDIUM(0.6),
    HIGH(0.9),
    CRITICAL(1.0)
}

enum class PatternType {
    VELOCITY_SPIKE,
    AMOUNT_ANOMALY,
    GEOGRAPHIC_SHIFT,
    TIME_ANOMALY
}

enum class UpdateType {
    PARAMETER_UPDATE,
    THRESHOLD_ADJUSTMENT,
    RULE_ACTIVATION,
    RULE_DEACTIVATION
}
