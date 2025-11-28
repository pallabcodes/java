package com.example.kotlinpay.payments.settlement

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
 * Payment Settlement Service
 *
 * Handles payment settlement and reconciliation with:
 * - Automated settlement processing
 * - Multi-gateway reconciliation
 * - Settlement status tracking
 * - Fee calculation and reporting
 * - Exception handling for failed settlements
 * - Regulatory reporting for settlements
 */
@Service
class SettlementService(
    private val auditLogger: AuditLogger
) {
    private val logger = LoggerFactory.getLogger(SettlementService::class.java)

    // Settlement records storage
    private val settlementRecords = ConcurrentHashMap<String, SettlementRecord>()

    // Daily settlement batches
    private val dailyBatches = ConcurrentHashMap<LocalDate, SettlementBatch>()

    // Reconciliation results
    private val reconciliationResults = CopyOnWriteArrayList<ReconciliationResult>()

    // Settlement configuration
    private val settlementConfig = SettlementConfig(
        settlementTime = "02:00", // 2 AM daily
        cutoffTime = "23:00", // 11 PM cutoff
        settlementDelay = 1, // 1 day delay
        reconciliationThreshold = BigDecimal("0.01") // $0.01 threshold for reconciliation
    )

    /**
     * Submit transaction for settlement
     */
    fun submitForSettlement(transaction: SettlementTransaction): SettlementResult {
        return try {
            logger.info("Submitting transaction ${transaction.transactionId} for settlement")

            // Create settlement record
            val settlementRecord = SettlementRecord(
                transactionId = transaction.transactionId,
                gatewayTransactionId = transaction.gatewayTransactionId,
                amount = transaction.amount,
                currency = transaction.currency,
                gateway = transaction.gateway,
                merchantId = transaction.merchantId,
                submittedAt = LocalDateTime.now(),
                settlementDate = calculateSettlementDate(),
                status = SettlementStatus.PENDING,
                fees = calculateSettlementFees(transaction),
                metadata = transaction.metadata
            )

            settlementRecords[transaction.transactionId] = settlementRecord

            // Add to daily batch
            addToDailyBatch(settlementRecord)

            auditLogger.logSecurityEvent(
                event = "SETTLEMENT_SUBMITTED",
                severity = "INFO",
                details = mapOf(
                    "transaction_id" to transaction.transactionId,
                    "amount" to transaction.amount.toString(),
                    "gateway" to transaction.gateway,
                    "settlement_date" to settlementRecord.settlementDate.toString()
                )
            )

            SettlementResult.Success(
                transactionId = transaction.transactionId,
                settlementId = settlementRecord.transactionId,
                estimatedSettlementDate = settlementRecord.settlementDate
            )

        } catch (e: Exception) {
            logger.error("Settlement submission failed for transaction: ${transaction.transactionId}", e)

            auditLogger.logSecurityEvent(
                event = "SETTLEMENT_SUBMISSION_FAILED",
                severity = "HIGH",
                details = mapOf(
                    "transaction_id" to transaction.transactionId,
                    "error" to e.message.toString()
                )
            )

            SettlementResult.Failure("Settlement submission failed: ${e.message}")
        }
    }

    /**
     * Process daily settlements (scheduled job)
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM daily
    fun processDailySettlements() {
        val settlementDate = LocalDate.now().minusDays(settlementConfig.settlementDelay.toLong())
        val batch = dailyBatches[settlementDate]

        if (batch == null || batch.records.isEmpty()) {
            logger.info("No settlements to process for date: $settlementDate")
            return
        }

        logger.info("Processing daily settlements for $settlementDate: ${batch.records.size} transactions")

        try {
            // Process settlements by gateway
            val gatewayBatches = batch.records.groupBy { it.gateway }

            gatewayBatches.forEach { (gateway, records) ->
                processGatewaySettlement(gateway, records, settlementDate)
            }

            // Mark batch as processed
            batch.status = BatchStatus.PROCESSED
            batch.processedAt = LocalDateTime.now()

            auditLogger.logSecurityEvent(
                event = "DAILY_SETTLEMENT_PROCESSED",
                severity = "INFO",
                details = mapOf(
                    "settlement_date" to settlementDate.toString(),
                    "total_transactions" to batch.records.size.toString(),
                    "total_amount" to batch.totalAmount.toString()
                )
            )

        } catch (e: Exception) {
            logger.error("Daily settlement processing failed for date: $settlementDate", e)

            auditLogger.logSecurityEvent(
                event = "DAILY_SETTLEMENT_FAILED",
                severity = "CRITICAL",
                details = mapOf(
                    "settlement_date" to settlementDate.toString(),
                    "error" to e.message.toString()
                )
            )
        }
    }

    /**
     * Reconcile settlements with bank statements
     */
    fun reconcileSettlements(bankStatement: BankStatement): ReconciliationResult {
        logger.info("Starting settlement reconciliation for period: ${bankStatement.periodStart} to ${bankStatement.periodEnd}")

        val periodSettlements = settlementRecords.values.filter { record ->
            record.settlementDate >= bankStatement.periodStart &&
            record.settlementDate <= bankStatement.periodEnd &&
            record.status == SettlementStatus.SETTLED
        }

        val totalSettledAmount = periodSettlements.sumOf { it.amount }
        val totalBankAmount = bankStatement.totalDeposits

        val difference = (totalSettledAmount - totalBankAmount).abs()

        val reconciliationResult = if (difference <= settlementConfig.reconciliationThreshold) {
            // Reconciliation successful
            ReconciliationResult.Success(
                periodStart = bankStatement.periodStart,
                periodEnd = bankStatement.periodEnd,
                transactionsReconciled = periodSettlements.size,
                totalAmount = totalSettledAmount,
                difference = difference,
                status = ReconciliationStatus.MATCHED
            )
        } else {
            // Reconciliation failed - find discrepancies
            val discrepancies = findDiscrepancies(periodSettlements, bankStatement.transactions)

            ReconciliationResult.DiscrepancyFound(
                periodStart = bankStatement.periodStart,
                periodEnd = bankStatement.periodEnd,
                transactionsReconciled = periodSettlements.size,
                totalAmount = totalSettledAmount,
                bankAmount = totalBankAmount,
                difference = difference,
                discrepancies = discrepancies,
                status = ReconciliationStatus.DISCREPANCY
            )
        }

        reconciliationResults.add(reconciliationResult)

        auditLogger.logSecurityEvent(
            event = "SETTLEMENT_RECONCILIATION_COMPLETED",
            severity = if (reconciliationResult is ReconciliationResult.Success) "INFO" else "WARNING",
            details = mapOf(
                "period_start" to bankStatement.periodStart.toString(),
                "period_end" to bankStatement.periodEnd.toString(),
                "transactions_reconciled" to periodSettlements.size.toString(),
                "difference" to difference.toString(),
                "status" to reconciliationResult.status.name
            )
        )

        return reconciliationResult
    }

    /**
     * Get settlement status for transaction
     */
    fun getSettlementStatus(transactionId: String): SettlementStatusResponse? {
        val record = settlementRecords[transactionId] ?: return null

        return SettlementStatusResponse(
            transactionId = record.transactionId,
            status = record.status,
            settlementDate = record.settlementDate,
            submittedAt = record.submittedAt,
            settledAt = record.settledAt,
            amount = record.amount,
            fees = record.fees,
            gateway = record.gateway
        )
    }

    /**
     * Generate settlement reports
     */
    fun generateSettlementReport(startDate: LocalDate, endDate: LocalDate): SettlementReport {
        val periodSettlements = settlementRecords.values.filter { record ->
            record.settlementDate >= startDate && record.settlementDate <= endDate
        }

        val totalAmount = periodSettlements.sumOf { it.amount }
        val totalFees = periodSettlements.sumOf { it.fees.totalFees }

        val byStatus = periodSettlements.groupBy { it.status }.mapValues { it.value.size }
        val byGateway = periodSettlements.groupBy { it.gateway }.mapValues { it.value.size }

        val successfulSettlements = periodSettlements.count { it.status == SettlementStatus.SETTLED }
        val failedSettlements = periodSettlements.count { it.status == SettlementStatus.FAILED }

        return SettlementReport(
            periodStart = startDate,
            periodEnd = endDate,
            totalTransactions = periodSettlements.size,
            totalAmount = totalAmount,
            totalFees = totalFees,
            netAmount = totalAmount - totalFees,
            successfulSettlements = successfulSettlements,
            failedSettlements = failedSettlements,
            settlementsByStatus = byStatus,
            settlementsByGateway = byGateway,
            generatedAt = LocalDateTime.now()
        )
    }

    /**
     * Handle settlement exceptions and failures
     */
    fun handleSettlementException(transactionId: String, exception: SettlementException): Boolean {
        return try {
            val record = settlementRecords[transactionId]
            if (record != null) {
                record.status = SettlementStatus.FAILED
                record.failureReason = exception.message
                record.failedAt = LocalDateTime.now()

                auditLogger.logSecurityEvent(
                    event = "SETTLEMENT_EXCEPTION_HANDLED",
                    severity = "HIGH",
                    details = mapOf(
                        "transaction_id" to transactionId,
                        "exception_type" to exception.type.name,
                        "reason" to exception.message
                    )
                )

                // Trigger exception handling workflow
                triggerExceptionWorkflow(record, exception)

                true
            } else {
                false
            }
        } catch (e: Exception) {
            logger.error("Failed to handle settlement exception for transaction: $transactionId", e)
            false
        }
    }

    /**
     * Private helper methods
     */

    private fun calculateSettlementDate(): LocalDate {
        val today = LocalDate.now()
        // Settlement typically occurs 1-2 business days after transaction
        return today.plusDays(settlementConfig.settlementDelay.toLong())
    }

    private fun calculateSettlementFees(transaction: SettlementTransaction): SettlementFees {
        // Simplified fee calculation - in production, this would vary by gateway and transaction type
        val gatewayFee = transaction.amount.multiply(BigDecimal("0.029")) // 2.9%
        val processingFee = BigDecimal("0.30") // Fixed fee
        val totalFees = gatewayFee.add(processingFee)

        return SettlementFees(
            gatewayFee = gatewayFee,
            processingFee = processingFee,
            totalFees = totalFees
        )
    }

    private fun addToDailyBatch(record: SettlementRecord) {
        val batchDate = record.settlementDate
        val batch = dailyBatches.getOrPut(batchDate) {
            SettlementBatch(
                date = batchDate,
                records = mutableListOf(),
                status = BatchStatus.PENDING
            )
        }

        batch.records.add(record)
        batch.totalAmount += record.amount
        batch.totalFees += record.fees.totalFees
    }

    private fun processGatewaySettlement(gateway: String, records: List<SettlementRecord>, settlementDate: LocalDate) {
        logger.info("Processing settlements for gateway: $gateway, transactions: ${records.size}")

        // In production, this would call actual gateway settlement APIs
        // For simulation, we'll mark all as settled
        records.forEach { record ->
            record.status = SettlementStatus.SETTLED
            record.settledAt = LocalDateTime.now()
        }

        logger.info("Successfully processed ${records.size} settlements for gateway: $gateway")
    }

    private fun findDiscrepancies(
        settlements: List<SettlementRecord>,
        bankTransactions: List<BankTransaction>
    ): List<Discrepancy> {
        val discrepancies = mutableListOf<Discrepancy>()

        // Group by transaction ID for comparison
        val settlementMap = settlements.associateBy { it.transactionId }
        val bankMap = bankTransactions.associateBy { it.transactionId }

        // Find missing transactions
        settlementMap.keys.forEach { transactionId ->
            if (!bankMap.containsKey(transactionId)) {
                discrepancies.add(Discrepancy(
                    transactionId = transactionId,
                    type = DiscrepancyType.MISSING_IN_BANK,
                    settlementAmount = settlementMap[transactionId]!!.amount,
                    bankAmount = BigDecimal.ZERO,
                    difference = settlementMap[transactionId]!!.amount
                ))
            }
        }

        // Find amount mismatches
        settlementMap.keys.intersect(bankMap.keys).forEach { transactionId ->
            val settlementAmount = settlementMap[transactionId]!!.amount
            val bankAmount = bankMap[transactionId]!!.amount

            if ((settlementAmount - bankAmount).abs() > settlementConfig.reconciliationThreshold) {
                discrepancies.add(Discrepancy(
                    transactionId = transactionId,
                    type = DiscrepancyType.AMOUNT_MISMATCH,
                    settlementAmount = settlementAmount,
                    bankAmount = bankAmount,
                    difference = (settlementAmount - bankAmount).abs()
                ))
            }
        }

        return discrepancies
    }

    private fun triggerExceptionWorkflow(record: SettlementRecord, exception: SettlementException) {
        // In production, this would trigger notifications, manual review processes, etc.
        logger.warn("Settlement exception workflow triggered for transaction: ${record.transactionId}")
    }

    /**
     * Get settlement service statistics
     */
    fun getSettlementStats(): SettlementStats {
        val now = LocalDateTime.now()
        val today = LocalDate.now()

        val todaysSettlements = settlementRecords.values.count {
            it.settlementDate == today && it.status == SettlementStatus.SETTLED
        }

        val pendingSettlements = settlementRecords.values.count { it.status == SettlementStatus.PENDING }
        val failedSettlements = settlementRecords.values.count { it.status == SettlementStatus.FAILED }

        val totalSettledAmount = settlementRecords.values
            .filter { it.status == SettlementStatus.SETTLED }
            .sumOf { it.amount }

        val totalFees = settlementRecords.values.sumOf { it.fees.totalFees }

        return SettlementStats(
            totalTransactions = settlementRecords.size,
            pendingSettlements = pendingSettlements,
            settledToday = todaysSettlements,
            failedSettlements = failedSettlements,
            totalSettledAmount = totalSettledAmount,
            totalFeesCollected = totalFees,
            reconciliationSuccessRate = calculateReconciliationSuccessRate()
        )
    }

    private fun calculateReconciliationSuccessRate(): Double {
        if (reconciliationResults.isEmpty()) return 0.0

        val successful = reconciliationResults.count {
            it is ReconciliationResult.Success || (it is ReconciliationResult.DiscrepancyFound && it.difference <= BigDecimal("1.00"))
        }

        return successful.toDouble() / reconciliationResults.size
    }
}

/**
 * Data classes for settlement service
 */

data class SettlementTransaction(
    val transactionId: String,
    val gatewayTransactionId: String,
    val amount: BigDecimal,
    val currency: String,
    val gateway: String,
    val merchantId: String,
    val metadata: Map<String, String> = emptyMap()
)

sealed class SettlementResult {
    data class Success(
        val transactionId: String,
        val settlementId: String,
        val estimatedSettlementDate: LocalDate
    ) : SettlementResult()

    data class Failure(val error: String) : SettlementResult()
}

data class SettlementRecord(
    val transactionId: String,
    val gatewayTransactionId: String,
    val amount: BigDecimal,
    val currency: String,
    val gateway: String,
    val merchantId: String,
    val submittedAt: LocalDateTime,
    val settlementDate: LocalDate,
    var status: SettlementStatus,
    val fees: SettlementFees,
    val metadata: Map<String, String> = emptyMap(),
    var settledAt: LocalDateTime? = null,
    var failedAt: LocalDateTime? = null,
    var failureReason: String? = null
)

data class SettlementFees(
    val gatewayFee: BigDecimal,
    val processingFee: BigDecimal,
    val totalFees: BigDecimal
)

data class SettlementBatch(
    val date: LocalDate,
    val records: MutableList<SettlementRecord>,
    var status: BatchStatus = BatchStatus.PENDING,
    var totalAmount: BigDecimal = BigDecimal.ZERO,
    var totalFees: BigDecimal = BigDecimal.ZERO,
    var processedAt: LocalDateTime? = null
)

sealed class ReconciliationResult {
    abstract val periodStart: LocalDate
    abstract val periodEnd: LocalDate
    abstract val transactionsReconciled: Int
    abstract val totalAmount: BigDecimal
    abstract val status: ReconciliationStatus

    data class Success(
        override val periodStart: LocalDate,
        override val periodEnd: LocalDate,
        override val transactionsReconciled: Int,
        override val totalAmount: BigDecimal,
        val difference: BigDecimal,
        override val status: ReconciliationStatus = ReconciliationStatus.MATCHED
    ) : ReconciliationResult()

    data class DiscrepancyFound(
        override val periodStart: LocalDate,
        override val periodEnd: LocalDate,
        override val transactionsReconciled: Int,
        override val totalAmount: BigDecimal,
        val bankAmount: BigDecimal,
        val difference: BigDecimal,
        val discrepancies: List<Discrepancy>,
        override val status: ReconciliationStatus = ReconciliationStatus.DISCREPANCY
    ) : ReconciliationResult()
}

data class BankStatement(
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val totalDeposits: BigDecimal,
    val transactions: List<BankTransaction>
)

data class BankTransaction(
    val transactionId: String,
    val amount: BigDecimal,
    val date: LocalDate
)

data class Discrepancy(
    val transactionId: String,
    val type: DiscrepancyType,
    val settlementAmount: BigDecimal,
    val bankAmount: BigDecimal,
    val difference: BigDecimal
)

data class SettlementStatusResponse(
    val transactionId: String,
    val status: SettlementStatus,
    val settlementDate: LocalDate,
    val submittedAt: LocalDateTime,
    val settledAt: LocalDateTime?,
    val amount: BigDecimal,
    val fees: SettlementFees,
    val gateway: String
)

data class SettlementReport(
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val totalTransactions: Int,
    val totalAmount: BigDecimal,
    val totalFees: BigDecimal,
    val netAmount: BigDecimal,
    val successfulSettlements: Int,
    val failedSettlements: Int,
    val settlementsByStatus: Map<SettlementStatus, Int>,
    val settlementsByGateway: Map<String, Int>,
    val generatedAt: LocalDateTime
)

data class SettlementException(
    val transactionId: String,
    val type: ExceptionType,
    override val message: String
) : RuntimeException(message)

data class SettlementConfig(
    val settlementTime: String,
    val cutoffTime: String,
    val settlementDelay: Int,
    val reconciliationThreshold: BigDecimal
)

data class SettlementStats(
    val totalTransactions: Int,
    val pendingSettlements: Int,
    val settledToday: Int,
    val failedSettlements: Int,
    val totalSettledAmount: BigDecimal,
    val totalFeesCollected: BigDecimal,
    val reconciliationSuccessRate: Double
)

enum class SettlementStatus {
    PENDING,
    PROCESSING,
    SETTLED,
    FAILED,
    CANCELLED
}

enum class BatchStatus {
    PENDING,
    PROCESSING,
    PROCESSED,
    FAILED
}

enum class ReconciliationStatus {
    MATCHED,
    DISCREPANCY,
    PENDING
}

enum class DiscrepancyType {
    MISSING_IN_BANK,
    MISSING_IN_SETTLEMENT,
    AMOUNT_MISMATCH,
    DATE_MISMATCH
}

enum class ExceptionType {
    GATEWAY_ERROR,
    INSUFFICIENT_FUNDS,
    COMPLIANCE_VIOLATION,
    TECHNICAL_ERROR,
    MANUAL_REVIEW_REQUIRED
}
