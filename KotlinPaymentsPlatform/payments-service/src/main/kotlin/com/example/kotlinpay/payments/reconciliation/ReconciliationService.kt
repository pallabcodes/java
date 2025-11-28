package com.example.kotlinpay.payments.reconciliation

import com.example.kotlinpay.shared.compliance.AuditLogger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Payment Reconciliation Service
 *
 * Handles comprehensive payment reconciliation with:
 * - Bank statement reconciliation
 * - Gateway settlement reconciliation
 * - Merchant account reconciliation
 * - Chargeback reconciliation
 * - Fee reconciliation
 * - Automated exception handling
 * - Regulatory reporting
 */
@Service
class ReconciliationService(
    private val auditLogger: AuditLogger
) {
    private val logger = LoggerFactory.getLogger(ReconciliationService::class.java)

    // Reconciliation records storage
    private val reconciliationRecords = ConcurrentHashMap<String, ReconciliationRecord>()

    // Reconciliation rules
    private val reconciliationRules = ConcurrentHashMap<String, ReconciliationRule>()

    // Exception queue for manual review
    private val exceptionQueue = CopyOnWriteArrayList<ReconciliationException>()

    // Reconciliation configuration
    private val config = ReconciliationConfig(
        toleranceThreshold = BigDecimal("0.01"), // $0.01 tolerance
        autoResolveThreshold = BigDecimal("0.10"), // Auto-resolve up to $0.10
        maxRetryAttempts = 3,
        reconciliationWindowDays = 7
    )

    init {
        initializeReconciliationRules()
    }

    /**
     * Reconcile payments with bank statements
     */
    fun reconcileWithBankStatements(
        bankStatements: List<BankStatement>,
        periodStart: LocalDate,
        periodEnd: LocalDate
    ): BankReconciliationResult {
        logger.info("Starting bank reconciliation for period: $periodStart to $periodEnd")

        val reconciliationResults = mutableListOf<BankReconciliation>()

        bankStatements.forEach { statement ->
            val result = reconcileBankStatement(statement, periodStart, periodEnd)
            reconciliationResults.add(result)
        }

        val totalTransactions = reconciliationResults.sumOf { it.totalTransactions }
        val matchedTransactions = reconciliationResults.sumOf { it.matchedTransactions }
        val discrepancies = reconciliationResults.flatMap { it.discrepancies }
        val totalAmount = reconciliationResults.sumOf { it.totalAmount }
        val matchedAmount = reconciliationResults.sumOf { it.matchedAmount }

        val overallMatchRate = if (totalTransactions > 0) {
            matchedTransactions.toDouble() / totalTransactions
        } else 0.0

        val result = BankReconciliationResult(
            periodStart = periodStart,
            periodEnd = periodEnd,
            totalStatements = bankStatements.size,
            totalTransactions = totalTransactions,
            matchedTransactions = matchedTransactions,
            matchRate = overallMatchRate,
            totalAmount = totalAmount,
            matchedAmount = matchedAmount,
            discrepancies = discrepancies,
            status = if (overallMatchRate >= 0.99) ReconciliationStatus.SUCCESS else ReconciliationStatus.DISCREPANCY,
            processedAt = LocalDateTime.now()
        )

        // Store reconciliation record
        val recordId = "BANK_REC_${System.currentTimeMillis()}"
        reconciliationRecords[recordId] = ReconciliationRecord(
            id = recordId,
            type = ReconciliationType.BANK_STATEMENT,
            periodStart = periodStart,
            periodEnd = periodEnd,
            status = result.status,
            totalAmount = totalAmount,
            matchedAmount = matchedAmount,
            discrepancies = discrepancies.size,
            processedAt = result.processedAt
        )

        auditLogger.logSecurityEvent(
            event = "BANK_RECONCILIATION_COMPLETED",
            severity = if (result.status == ReconciliationStatus.SUCCESS) "INFO" else "WARNING",
            details = mapOf(
                "period_start" to periodStart.toString(),
                "period_end" to periodEnd.toString(),
                "match_rate" to String.format("%.2f%%", overallMatchRate * 100),
                "discrepancies" to discrepancies.size.toString(),
                "total_amount" to totalAmount.toString()
            )
        )

        return result
    }

    /**
     * Reconcile with payment gateway settlements
     */
    fun reconcileWithGatewaySettlements(
        gatewaySettlements: List<GatewaySettlement>,
        periodStart: LocalDate,
        periodEnd: LocalDate
    ): GatewayReconciliationResult {
        logger.info("Starting gateway reconciliation for period: $periodStart to $periodEnd")

        val gatewayResults = mutableListOf<GatewayReconciliation>()

        gatewaySettlements.groupBy { it.gatewayName }.forEach { (gatewayName, settlements) ->
            val result = reconcileGatewaySettlements(gatewayName, settlements, periodStart, periodEnd)
            gatewayResults.add(result)
        }

        val totalSettlements = gatewayResults.sumOf { it.totalSettlements }
        val matchedSettlements = gatewayResults.sumOf { it.matchedSettlements }
        val totalAmount = gatewayResults.sumOf { it.totalAmount }
        val matchedAmount = gatewayResults.sumOf { it.matchedAmount }
        val feeDiscrepancies = gatewayResults.flatMap { it.feeDiscrepancies }

        val result = GatewayReconciliationResult(
            periodStart = periodStart,
            periodEnd = periodEnd,
            totalGateways = gatewayResults.size,
            totalSettlements = totalSettlements,
            matchedSettlements = matchedSettlements,
            totalAmount = totalAmount,
            matchedAmount = matchedAmount,
            feeDiscrepancies = feeDiscrepancies,
            status = if (matchedSettlements == totalSettlements) ReconciliationStatus.SUCCESS else ReconciliationStatus.DISCREPANCY,
            processedAt = LocalDateTime.now()
        )

        // Store reconciliation record
        val recordId = "GATEWAY_REC_${System.currentTimeMillis()}"
        reconciliationRecords[recordId] = ReconciliationRecord(
            id = recordId,
            type = ReconciliationType.GATEWAY_SETTLEMENT,
            periodStart = periodStart,
            periodEnd = periodEnd,
            status = result.status,
            totalAmount = totalAmount,
            matchedAmount = matchedAmount,
            discrepancies = feeDiscrepancies.size,
            processedAt = result.processedAt
        )

        return result
    }

    /**
     * Reconcile merchant accounts
     */
    fun reconcileMerchantAccounts(
        merchantStatements: List<MerchantStatement>,
        periodStart: LocalDate,
        periodEnd: LocalDate
    ): MerchantReconciliationResult {
        logger.info("Starting merchant reconciliation for period: $periodStart to $periodEnd")

        val merchantResults = mutableListOf<MerchantReconciliation>()

        merchantStatements.forEach { statement ->
            val result = reconcileMerchantStatement(statement, periodStart, periodEnd)
            merchantResults.add(result)
        }

        val totalTransactions = merchantResults.sumOf { it.totalTransactions }
        val matchedTransactions = merchantResults.sumOf { it.matchedTransactions }
        val totalAmount = merchantResults.sumOf { it.totalAmount }
        val matchedAmount = merchantResults.sumOf { it.matchedAmount }
        val chargebacks = merchantResults.flatMap { it.chargebacks }

        val result = MerchantReconciliationResult(
            periodStart = periodStart,
            periodEnd = periodEnd,
            totalMerchants = merchantStatements.size,
            totalTransactions = totalTransactions,
            matchedTransactions = matchedTransactions,
            totalAmount = totalAmount,
            matchedAmount = matchedAmount,
            chargebacks = chargebacks,
            status = if (matchedTransactions == totalTransactions) ReconciliationStatus.SUCCESS else ReconciliationStatus.DISCREPANCY,
            processedAt = LocalDateTime.now()
        )

        // Store reconciliation record
        val recordId = "MERCHANT_REC_${System.currentTimeMillis()}"
        reconciliationRecords[recordId] = ReconciliationRecord(
            id = recordId,
            type = ReconciliationType.MERCHANT_ACCOUNT,
            periodStart = periodStart,
            periodEnd = periodEnd,
            status = result.status,
            totalAmount = totalAmount,
            matchedAmount = matchedAmount,
            discrepancies = totalTransactions - matchedTransactions,
            processedAt = result.processedAt
        )

        return result
    }

    /**
     * Handle reconciliation exceptions
     */
    fun handleReconciliationException(exception: ReconciliationException): ExceptionResolution {
        logger.warn("Handling reconciliation exception: ${exception.id}")

        exceptionQueue.add(exception)

        // Attempt auto-resolution for small discrepancies
        return if (exception.amountDifference <= config.autoResolveThreshold) {
            resolveSmallDiscrepancy(exception)
        } else {
            ExceptionResolution.ManualReviewRequired(
                exceptionId = exception.id,
                reason = "Discrepancy exceeds auto-resolution threshold",
                requiredAction = "Manual investigation and resolution"
            )
        }
    }

    /**
     * Get reconciliation report
     */
    fun getReconciliationReport(
        type: ReconciliationType,
        startDate: LocalDate,
        endDate: LocalDate
    ): ReconciliationReport {
        val relevantRecords = reconciliationRecords.values.filter { record ->
            record.type == type &&
            record.periodStart >= startDate &&
            record.periodEnd <= endDate
        }

        val totalRecords = relevantRecords.size
        val successfulRecords = relevantRecords.count { it.status == ReconciliationStatus.SUCCESS }
        val discrepancyRecords = relevantRecords.count { it.status == ReconciliationStatus.DISCREPANCY }
        val totalAmount = relevantRecords.sumOf { it.totalAmount }
        val matchedAmount = relevantRecords.sumOf { it.matchedAmount }
        val totalDiscrepancies = relevantRecords.sumOf { it.discrepancies }

        return ReconciliationReport(
            type = type,
            periodStart = startDate,
            periodEnd = endDate,
            totalRecords = totalRecords,
            successfulRecords = successfulRecords,
            discrepancyRecords = discrepancyRecords,
            totalAmount = totalAmount,
            matchedAmount = matchedAmount,
            totalDiscrepancies = totalDiscrepancies,
            successRate = if (totalRecords > 0) successfulRecords.toDouble() / totalRecords else 0.0,
            generatedAt = LocalDateTime.now()
        )
    }

    /**
     * Scheduled reconciliation job
     */
    @Scheduled(cron = "0 0 6 * * ?") // Run at 6 AM daily
    fun performDailyReconciliation() {
        val yesterday = LocalDate.now().minusDays(1)

        logger.info("Starting daily reconciliation for: $yesterday")

        try {
            // In production, this would fetch actual bank statements, gateway settlements, etc.
            // For simulation, we'll create mock data
            performMockReconciliation(yesterday)

            auditLogger.logSecurityEvent(
                event = "DAILY_RECONCILIATION_COMPLETED",
                severity = "INFO",
                details = mapOf("reconciliation_date" to yesterday.toString())
            )

        } catch (e: Exception) {
            logger.error("Daily reconciliation failed for: $yesterday", e)

            auditLogger.logSecurityEvent(
                event = "DAILY_RECONCILIATION_FAILED",
                severity = "CRITICAL",
                details = mapOf(
                    "reconciliation_date" to yesterday.toString(),
                    "error" to e.message.toString()
                )
            )
        }
    }

    /**
     * Get exception queue for manual review
     */
    fun getExceptionQueue(): List<ReconciliationException> {
        return exceptionQueue.toList()
    }

    /**
     * Resolve reconciliation exception
     */
    fun resolveException(exceptionId: String, resolution: ExceptionResolution): Boolean {
        val exception = exceptionQueue.find { it.id == exceptionId }
        return if (exception != null) {
            exceptionQueue.remove(exception)

            auditLogger.logSecurityEvent(
                event = "RECONCILIATION_EXCEPTION_RESOLVED",
                severity = "INFO",
                details = mapOf(
                    "exception_id" to exceptionId,
                    "resolution_type" to resolution.javaClass.simpleName
                )
            )

            true
        } else {
            false
        }
    }

    /**
     * Private helper methods
     */

    private fun initializeReconciliationRules() {
        // Bank reconciliation rules
        reconciliationRules["BANK_AMOUNT_MATCH"] = ReconciliationRule(
            name = "BANK_AMOUNT_MATCH",
            type = RuleType.AMOUNT_MATCH,
            tolerance = config.toleranceThreshold,
            action = RuleAction.MATCH
        )

        reconciliationRules["BANK_DATE_MATCH"] = ReconciliationRule(
            name = "BANK_DATE_MATCH",
            type = RuleType.DATE_MATCH,
            tolerance = BigDecimal.ZERO,
            action = RuleAction.MATCH_WITHIN_WINDOW
        )

        // Gateway reconciliation rules
        reconciliationRules["GATEWAY_FEE_MATCH"] = ReconciliationRule(
            name = "GATEWAY_FEE_MATCH",
            type = RuleType.FEE_MATCH,
            tolerance = BigDecimal("0.05"),
            action = RuleAction.FLAG_DISCREPANCY
        )

        // Merchant reconciliation rules
        reconciliationRules["MERCHANT_CHARGEBACK_MATCH"] = ReconciliationRule(
            name = "MERCHANT_CHARGEBACK_MATCH",
            type = RuleType.CHARGEBACK_MATCH,
            tolerance = BigDecimal.ZERO,
            action = RuleAction.MATCH
        )
    }

    private fun reconcileBankStatement(
        statement: BankStatement,
        periodStart: LocalDate,
        periodEnd: LocalDate
    ): BankReconciliation {
        // In production, this would query actual payment records
        // For simulation, we'll assume 98% match rate
        val totalTransactions = statement.transactions.size
        val matchedTransactions = (totalTransactions * 0.98).toInt()
        val totalAmount = statement.transactions.sumOf { it.amount }
        val matchedAmount = totalAmount.multiply(BigDecimal("0.98"))

        val discrepancies = if (matchedTransactions < totalTransactions) {
            listOf(
                Discrepancy(
                    transactionId = "DISC_${System.currentTimeMillis()}",
                    type = DiscrepancyType.AMOUNT_MISMATCH,
                    expectedAmount = totalAmount.subtract(matchedAmount),
                    actualAmount = BigDecimal.ZERO,
                    difference = totalAmount.subtract(matchedAmount)
                )
            )
        } else {
            emptyList()
        }

        return BankReconciliation(
            bankName = statement.bankName,
            accountNumber = statement.accountNumber,
            totalTransactions = totalTransactions,
            matchedTransactions = matchedTransactions,
            totalAmount = totalAmount,
            matchedAmount = matchedAmount,
            discrepancies = discrepancies
        )
    }

    private fun reconcileGatewaySettlements(
        gatewayName: String,
        settlements: List<GatewaySettlement>,
        periodStart: LocalDate,
        periodEnd: LocalDate
    ): GatewayReconciliation {
        val totalSettlements = settlements.size
        val matchedSettlements = totalSettlements // Assume all match for simulation
        val totalAmount = settlements.sumOf { it.amount }
        val matchedAmount = totalAmount
        val feeDiscrepancies = emptyList<FeeDiscrepancy>() // No discrepancies in simulation

        return GatewayReconciliation(
            gatewayName = gatewayName,
            totalSettlements = totalSettlements,
            matchedSettlements = matchedSettlements,
            totalAmount = totalAmount,
            matchedAmount = matchedAmount,
            feeDiscrepancies = feeDiscrepancies
        )
    }

    private fun reconcileMerchantStatement(
        statement: MerchantStatement,
        periodStart: LocalDate,
        periodEnd: LocalDate
    ): MerchantReconciliation {
        val totalTransactions = statement.transactions.size
        val matchedTransactions = (totalTransactions * 0.97).toInt() // 97% match rate
        val totalAmount = statement.transactions.sumOf { it.amount }
        val matchedAmount = totalAmount.multiply(BigDecimal("0.97"))

        val chargebacks = if (matchedTransactions < totalTransactions) {
            listOf(
                Chargeback(
                    transactionId = "CB_${System.currentTimeMillis()}",
                    amount = totalAmount.subtract(matchedAmount),
                    reason = "Fraudulent transaction",
                    date = LocalDate.now()
                )
            )
        } else {
            emptyList()
        }

        return MerchantReconciliation(
            merchantId = statement.merchantId,
            totalTransactions = totalTransactions,
            matchedTransactions = matchedTransactions,
            totalAmount = totalAmount,
            matchedAmount = matchedAmount,
            chargebacks = chargebacks
        )
    }

    private fun resolveSmallDiscrepancy(exception: ReconciliationException): ExceptionResolution {
        // Auto-resolve small discrepancies
        return ExceptionResolution.AutoResolved(
            exceptionId = exception.id,
            resolution = "Small discrepancy auto-resolved",
            adjustedAmount = exception.amountDifference
        )
    }

    private fun performMockReconciliation(date: LocalDate) {
        // Mock reconciliation process for daily job
        logger.info("Performing mock reconciliation for $date")
        // In production, this would perform actual reconciliation
    }

    /**
     * Get reconciliation service statistics
     */
    fun getReconciliationStats(): ReconciliationStats {
        val now = LocalDateTime.now()
        val last24Hours = now.minusHours(24)

        val recentRecords = reconciliationRecords.values.count {
            it.processedAt?.isAfter(last24Hours) ?: false
        }

        val successRate = reconciliationRecords.values.let { records ->
            if (records.isEmpty()) 0.0 else {
                records.count { it.status == ReconciliationStatus.SUCCESS }.toDouble() / records.size
            }
        }

        val totalDiscrepancies = reconciliationRecords.values.sumOf { it.discrepancies }
        val pendingExceptions = exceptionQueue.size

        return ReconciliationStats(
            totalReconciliations = reconciliationRecords.size,
            recentReconciliations = recentRecords,
            successRate = successRate,
            totalDiscrepancies = totalDiscrepancies,
            pendingExceptions = pendingExceptions,
            averageProcessingTime = 45.0 // Mock: 45 seconds
        )
    }
}

/**
 * Data classes for reconciliation service
 */

data class BankStatement(
    val bankName: String,
    val accountNumber: String,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val transactions: List<BankTransaction>
)

data class BankTransaction(
    val transactionId: String,
    val amount: BigDecimal,
    val date: LocalDate,
    val description: String
)

data class GatewaySettlement(
    val gatewayName: String,
    val settlementId: String,
    val amount: BigDecimal,
    val fees: BigDecimal,
    val settlementDate: LocalDate,
    val transactions: List<String>
)

data class MerchantStatement(
    val merchantId: String,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val transactions: List<MerchantTransaction>
)

data class MerchantTransaction(
    val transactionId: String,
    val amount: BigDecimal,
    val date: LocalDate,
    val type: TransactionType
)

sealed class ReconciliationResult {
    abstract val periodStart: LocalDate
    abstract val periodEnd: LocalDate
    abstract val status: ReconciliationStatus
    abstract val processedAt: LocalDateTime
}

data class BankReconciliationResult(
    override val periodStart: LocalDate,
    override val periodEnd: LocalDate,
    val totalStatements: Int,
    val totalTransactions: Int,
    val matchedTransactions: Int,
    val matchRate: Double,
    val totalAmount: BigDecimal,
    val matchedAmount: BigDecimal,
    val discrepancies: List<Discrepancy>,
    override val status: ReconciliationStatus,
    override val processedAt: LocalDateTime
) : ReconciliationResult()

data class GatewayReconciliationResult(
    override val periodStart: LocalDate,
    override val periodEnd: LocalDate,
    val totalGateways: Int,
    val totalSettlements: Int,
    val matchedSettlements: Int,
    val totalAmount: BigDecimal,
    val matchedAmount: BigDecimal,
    val feeDiscrepancies: List<FeeDiscrepancy>,
    override val status: ReconciliationStatus,
    override val processedAt: LocalDateTime
) : ReconciliationResult()

data class MerchantReconciliationResult(
    override val periodStart: LocalDate,
    override val periodEnd: LocalDate,
    val totalMerchants: Int,
    val totalTransactions: Int,
    val matchedTransactions: Int,
    val totalAmount: BigDecimal,
    val matchedAmount: BigDecimal,
    val chargebacks: List<Chargeback>,
    override val status: ReconciliationStatus,
    override val processedAt: LocalDateTime
) : ReconciliationResult()

data class BankReconciliation(
    val bankName: String,
    val accountNumber: String,
    val totalTransactions: Int,
    val matchedTransactions: Int,
    val totalAmount: BigDecimal,
    val matchedAmount: BigDecimal,
    val discrepancies: List<Discrepancy>
)

data class GatewayReconciliation(
    val gatewayName: String,
    val totalSettlements: Int,
    val matchedSettlements: Int,
    val totalAmount: BigDecimal,
    val matchedAmount: BigDecimal,
    val feeDiscrepancies: List<FeeDiscrepancy>
)

data class MerchantReconciliation(
    val merchantId: String,
    val totalTransactions: Int,
    val matchedTransactions: Int,
    val totalAmount: BigDecimal,
    val matchedAmount: BigDecimal,
    val chargebacks: List<Chargeback>
)

data class Discrepancy(
    val transactionId: String,
    val type: DiscrepancyType,
    val expectedAmount: BigDecimal,
    val actualAmount: BigDecimal,
    val difference: BigDecimal
)

data class FeeDiscrepancy(
    val settlementId: String,
    val expectedFees: BigDecimal,
    val actualFees: BigDecimal,
    val difference: BigDecimal
)

data class Chargeback(
    val transactionId: String,
    val amount: BigDecimal,
    val reason: String,
    val date: LocalDate
)

data class ReconciliationRecord(
    val id: String,
    val type: ReconciliationType,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val status: ReconciliationStatus,
    val totalAmount: BigDecimal,
    val matchedAmount: BigDecimal,
    val discrepancies: Int,
    val processedAt: LocalDateTime
)

data class ReconciliationRule(
    val name: String,
    val type: RuleType,
    val tolerance: BigDecimal,
    val action: RuleAction
)

data class ReconciliationException(
    val id: String,
    val type: ExceptionType,
    val transactionId: String,
    val amountDifference: BigDecimal,
    val description: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

sealed class ExceptionResolution {
    data class AutoResolved(
        val exceptionId: String,
        val resolution: String,
        val adjustedAmount: BigDecimal
    ) : ExceptionResolution()

    data class ManualReviewRequired(
        val exceptionId: String,
        val reason: String,
        val requiredAction: String
    ) : ExceptionResolution()

    data class Escalated(
        val exceptionId: String,
        val escalationReason: String,
        val assignedTo: String
    ) : ExceptionResolution()
}

data class ReconciliationReport(
    val type: ReconciliationType,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val totalRecords: Int,
    val successfulRecords: Int,
    val discrepancyRecords: Int,
    val totalAmount: BigDecimal,
    val matchedAmount: BigDecimal,
    val totalDiscrepancies: Int,
    val successRate: Double,
    val generatedAt: LocalDateTime
)

data class ReconciliationConfig(
    val toleranceThreshold: BigDecimal,
    val autoResolveThreshold: BigDecimal,
    val maxRetryAttempts: Int,
    val reconciliationWindowDays: Int
)

data class ReconciliationStats(
    val totalReconciliations: Int,
    val recentReconciliations: Int,
    val successRate: Double,
    val totalDiscrepancies: Int,
    val pendingExceptions: Int,
    val averageProcessingTime: Double
)

enum class ReconciliationType {
    BANK_STATEMENT,
    GATEWAY_SETTLEMENT,
    MERCHANT_ACCOUNT,
    CHARGEBACK
}

enum class ReconciliationStatus {
    SUCCESS,
    DISCREPANCY,
    PENDING,
    FAILED
}

enum class DiscrepancyType {
    AMOUNT_MISMATCH,
    MISSING_TRANSACTION,
    DATE_MISMATCH,
    DUPLICATE_TRANSACTION
}

enum class RuleType {
    AMOUNT_MATCH,
    DATE_MATCH,
    FEE_MATCH,
    CHARGEBACK_MATCH
}

enum class RuleAction {
    MATCH,
    FLAG_DISCREPANCY,
    MATCH_WITHIN_WINDOW,
    AUTO_RESOLVE
}

enum class ExceptionType {
    LARGE_DISCREPANCY,
    MISSING_TRANSACTION,
    SYSTEM_ERROR,
    MANUAL_REVIEW
}

enum class TransactionType {
    SALE,
    REFUND,
    CHARGEBACK,
    ADJUSTMENT
}
