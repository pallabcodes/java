package com.example.ledgerpay.core.data.offline

import com.example.ledgerpay.core.data.prefs.SecureStorage
import com.example.ledgerpay.core.network.CreateIntentRequest
import com.example.ledgerpay.core.network.CreatePaymentRequest
import com.example.ledgerpay.core.network.CreateTransferRequest
import com.example.ledgerpay.core.network.PaymentsApi
import kotlinx.coroutines.delay
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineSyncProcessor @Inject constructor(
    private val transactionQueue: OfflineTransactionQueue,
    private val secureStorage: SecureStorage,
    private val paymentsApi: PaymentsApi
) {

    companion object {
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val MAX_CONSECUTIVE_FAILURES = 5
        private const val DEFAULT_CURRENCY = "USD"
        private const val MAX_ALLOWED_MINOR_AMOUNT = 1_000_000_00L
        private const val BASE_RETRY_BACKOFF_MS = 250L
        private const val MAX_RETRY_BACKOFF_MS = 5_000L
    }

    suspend fun syncPendingTransactions(
        batchSize: Int,
        onProgress: (completed: Int, total: Int) -> Unit = { _, _ -> }
    ): Result<SyncResult> {
        return runCatching {
            val pendingTransactions = transactionQueue.getAllPendingTransactions()
            val totalTransactions = pendingTransactions.size

            if (totalTransactions == 0) {
                return@runCatching SyncResult(0, 0, 0, emptyList())
            }

            var successCount = 0
            var failureCount = 0
            var consecutiveFailures = 0
            val failedTransactions = mutableListOf<String>()
            val batches = pendingTransactions.chunked(batchSize)
            var circuitOpen = false

            for ((batchIndex, batch) in batches.withIndex()) {
                val batchStart = batchIndex * batchSize
                for ((transactionIndex, transaction) in batch.withIndex()) {
                    val completed = batchStart + transactionIndex + 1
                    onProgress(completed, totalTransactions)
                    transactionQueue.updateTransactionStatus(transaction.id, TransactionStatus.PROCESSING)

                    val syncResult = syncTransaction(transaction)
                    if (syncResult.isSuccess) {
                        transactionQueue.updateTransactionStatus(transaction.id, TransactionStatus.COMPLETED)
                        successCount++
                        consecutiveFailures = 0
                    } else {
                        handleSyncFailure(transaction, syncResult.exceptionOrNull())
                        failureCount++
                        failedTransactions.add(transaction.id)

                        consecutiveFailures++
                        if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                            circuitOpen = true
                            break
                        }

                        val retryAttempt = (transaction.retryCount + 1).coerceAtLeast(1)
                        delay(calculateRetryBackoffMs(retryAttempt))
                    }
                }

                if (circuitOpen) {
                    break
                }

                // Avoid request bursts across large queues.
                delay(100)
            }

            val cleanedCount = transactionQueue.clearProcessedTransactions()
            secureStorage.saveLastSyncTime(System.currentTimeMillis())
            SyncResult(successCount, failureCount, cleanedCount, failedTransactions)
        }
    }

    private suspend fun syncTransaction(transaction: OfflineTransaction): Result<Unit> {
        return when (transaction.type) {
            TransactionType.PAYMENT -> syncPaymentTransaction(transaction)
            TransactionType.TRANSFER -> syncTransferTransaction(transaction)
            TransactionType.REQUEST -> syncRequestTransaction(transaction)
        }
    }

    private suspend fun syncPaymentTransaction(transaction: OfflineTransaction): Result<Unit> {
        return runCatching {
            require(transaction.amount > 0) { "Transaction amount must be positive" }
            val amountMinor = toMinorAmount(transaction.amount)
            val idempotencyKey = idempotencyKeyFor(transaction)
            paymentsApi.createIntent(idempotencyKey, CreateIntentRequest(amountMinor, DEFAULT_CURRENCY))
            Unit
        }
    }

    private suspend fun syncTransferTransaction(transaction: OfflineTransaction): Result<Unit> {
        return runCatching {
            require(transaction.amount > 0) { "Transfer amount must be positive" }
            val amountMinor = toMinorAmount(transaction.amount)
            val idempotencyKey = idempotencyKeyFor("transfer", transaction)
            paymentsApi.createTransfer(
                idempotencyKey = idempotencyKey,
                req = CreateTransferRequest(
                    amountMinor = amountMinor,
                    currency = DEFAULT_CURRENCY,
                    recipient = transaction.recipient,
                    note = transaction.description
                )
            )
            Unit
        }
    }

    private suspend fun syncRequestTransaction(transaction: OfflineTransaction): Result<Unit> {
        return runCatching {
            require(transaction.amount > 0) { "Request amount must be positive" }
            val amountMinor = toMinorAmount(transaction.amount)
            val idempotencyKey = idempotencyKeyFor("request", transaction)
            paymentsApi.createPaymentRequest(
                idempotencyKey = idempotencyKey,
                req = CreatePaymentRequest(
                    amountMinor = amountMinor,
                    currency = DEFAULT_CURRENCY,
                    payee = transaction.recipient,
                    memo = transaction.description
                )
            )
            Unit
        }
    }

    private suspend fun handleSyncFailure(transaction: OfflineTransaction, error: Throwable?) {
        val errorMessage = error?.message ?: "Unknown sync error"
        transactionQueue.incrementRetryCount(transaction.id, errorMessage)
        val newRetryCount = transaction.retryCount + 1

        if (newRetryCount >= MAX_RETRY_ATTEMPTS) {
            transactionQueue.updateTransactionStatus(transaction.id, TransactionStatus.FAILED)
        } else {
            transactionQueue.updateTransactionStatus(transaction.id, TransactionStatus.PENDING)
        }
    }

    private fun idempotencyKeyFor(transaction: OfflineTransaction): String {
        return idempotencyKeyFor(transaction.type.name.lowercase(), transaction)
    }

    private fun idempotencyKeyFor(kind: String, transaction: OfflineTransaction): String {
        return "offline-sync-$kind-${transaction.id}"
    }

    private fun toMinorAmount(amount: Double): Long {
        require(amount.isFinite()) { "Amount must be finite" }
        val minor = BigDecimal.valueOf(amount)
            .movePointRight(2)
            .setScale(0, RoundingMode.HALF_UP)

        val amountMinor = minor.longValueExact()
        require(amountMinor in 1..MAX_ALLOWED_MINOR_AMOUNT) {
            "Amount out of supported range"
        }
        return amountMinor
    }

    private fun calculateRetryBackoffMs(retryAttempt: Int): Long {
        val boundedAttempt = retryAttempt.coerceIn(1, 6)
        val exponentialBackoff = BASE_RETRY_BACKOFF_MS * (1L shl (boundedAttempt - 1))
        val cappedBackoff = exponentialBackoff.coerceAtMost(MAX_RETRY_BACKOFF_MS)
        val jitterWindow = (cappedBackoff / 5).coerceAtLeast(1L)
        val jitter = Random.nextLong(-jitterWindow, jitterWindow + 1L)
        return (cappedBackoff + jitter).coerceIn(BASE_RETRY_BACKOFF_MS, MAX_RETRY_BACKOFF_MS)
    }
}
