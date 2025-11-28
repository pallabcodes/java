package com.example.ledgerpay.core.data.offline

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.work.*
import com.example.ledgerpay.core.data.prefs.SecureStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncService @Inject constructor(
    private val context: Context,
    private val transactionQueue: OfflineTransactionQueue,
    private val secureStorage: SecureStorage
) {

    companion object {
        private const val SYNC_WORK_NAME = "offline_transaction_sync"
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val SYNC_INTERVAL_MINUTES = 15L
        private const val BATCH_SIZE = 10
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val workManager = WorkManager.getInstance(context)

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus = _syncStatus.asStateFlow()

    private val _syncProgress = MutableStateFlow<SyncProgress?>(null)
    val syncProgress = _syncProgress.asStateFlow()

    init {
        // Schedule periodic sync
        schedulePeriodicSync()

        // Listen for network changes
        registerNetworkCallback()
    }

    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncWork = PeriodicWorkRequestBuilder<SyncWorker>(SYNC_INTERVAL_MINUTES, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncWork
        )
    }

    suspend fun syncNow(): Result<SyncResult> {
        return withContext(Dispatchers.IO) {
            try {
                if (!isNetworkAvailable()) {
                    return@withContext Result.failure(Exception("No network connection"))
                }

                _syncStatus.value = SyncStatus.Syncing
                _syncProgress.value = SyncProgress(0, 0)

                val pendingTransactions = transactionQueue.getAllPendingTransactions()
                val totalTransactions = pendingTransactions.size

                if (totalTransactions == 0) {
                    _syncStatus.value = SyncStatus.Idle
                    _syncProgress.value = null
                    return@withContext Result.success(SyncResult(0, 0, 0, emptyList()))
                }

                var successCount = 0
                var failureCount = 0
                val failedTransactions = mutableListOf<String>()

                // Process transactions in batches
                pendingTransactions.chunked(BATCH_SIZE).forEachIndexed { batchIndex, batch ->
                    val batchStart = batchIndex * BATCH_SIZE
                    val batchEnd = minOf(batchStart + batch.size, totalTransactions)

                    batch.forEachIndexed { transactionIndex, transaction ->
                        val overallProgress = batchStart + transactionIndex + 1
                        _syncProgress.value = SyncProgress(overallProgress, totalTransactions)

                        try {
                            val syncResult = syncTransaction(transaction)
                            if (syncResult.isSuccess) {
                                transactionQueue.updateTransactionStatus(transaction.id, TransactionStatus.COMPLETED)
                                successCount++
                            } else {
                                handleSyncFailure(transaction)
                                failureCount++
                                failedTransactions.add(transaction.id)
                            }
                        } catch (e: Exception) {
                            handleSyncFailure(transaction)
                            failureCount++
                            failedTransactions.add(transaction.id)
                        }
                    }

                    // Small delay between batches to avoid overwhelming the server
                    delay(1000)
                }

                // Clean up processed transactions
                val cleanedCount = transactionQueue.clearProcessedTransactions()

                val result = SyncResult(successCount, failureCount, cleanedCount, failedTransactions)

                _syncStatus.value = if (failureCount > 0) SyncStatus.PartialSuccess else SyncStatus.Idle
                _syncProgress.value = null

                Result.success(result)

            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.Error(e.message ?: "Unknown error")
                _syncProgress.value = null
                Result.failure(e)
            }
        }
    }

    private suspend fun syncTransaction(transaction: OfflineTransaction): Result<Unit> {
        return try {
            // Simulate API call - replace with actual payment API integration
            when (transaction.type) {
                TransactionType.PAYMENT -> syncPaymentTransaction(transaction)
                TransactionType.TRANSFER -> syncTransferTransaction(transaction)
                TransactionType.REQUEST -> syncRequestTransaction(transaction)
            }

            // Simulate network delay and potential failures
            delay((500..2000).random().toLong())

            // Simulate random failures (10% failure rate)
            if (kotlin.random.Random.nextFloat() < 0.1f) {
                throw Exception("Simulated network error")
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun syncPaymentTransaction(transaction: OfflineTransaction): Result<Unit> {
        // TODO: Implement actual payment processing API call
        // Example: paymentApi.processPayment(transaction.toPaymentRequest())
        delay(1000) // Simulate API call
        return Result.success(Unit)
    }

    private suspend fun syncTransferTransaction(transaction: OfflineTransaction): Result<Unit> {
        // TODO: Implement actual transfer processing API call
        delay(800) // Simulate API call
        return Result.success(Unit)
    }

    private suspend fun syncRequestTransaction(transaction: OfflineTransaction): Result<Unit> {
        // TODO: Implement actual payment request API call
        delay(600) // Simulate API call
        return Result.success(Unit)
    }

    private suspend fun handleSyncFailure(transaction: OfflineTransaction) {
        val newRetryCount = transaction.retryCount + 1

        if (newRetryCount >= MAX_RETRY_ATTEMPTS) {
            transactionQueue.updateTransactionStatus(transaction.id, TransactionStatus.FAILED)
        } else {
            // Update retry count but keep as pending for next sync attempt
            // Note: This would require modifying the queue to support updating retry count
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }

    private fun registerNetworkCallback() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                super.onAvailable(network)
                // Trigger sync when network becomes available
                scope.launch {
                    if (transactionQueue.getTransactionCount() > 0) {
                        syncNow()
                    }
                }
            }

            override fun onLost(network: android.net.Network) {
                super.onLost(network)
                _syncStatus.value = SyncStatus.NoNetwork
            }
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    suspend fun getSyncStatistics(): SyncStatistics {
        val totalTransactions = transactionQueue.getTransactionCount()
        val pendingTransactions = transactionQueue.getAllPendingTransactions().size

        return SyncStatistics(
            totalQueued = totalTransactions,
            pendingSync = pendingTransactions,
            lastSyncTime = secureStorage.getLastSyncTime(),
            syncSuccessRate = calculateSuccessRate()
        )
    }

    private fun calculateSuccessRate(): Double {
        // This would track success/failure rates over time
        // For now, return a mock value
        return 0.95
    }

    fun cancelSync() {
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
        _syncStatus.value = SyncStatus.Idle
        _syncProgress.value = null
    }

    suspend fun cleanup() {
        scope.cancel()
        cancelSync()
    }
}

// Supporting data classes
data class SyncResult(
    val successCount: Int,
    val failureCount: Int,
    val cleanedCount: Int,
    val failedTransactionIds: List<String>
)

data class SyncProgress(
    val completed: Int,
    val total: Int
)

data class SyncStatistics(
    val totalQueued: Int,
    val pendingSync: Int,
    val lastSyncTime: Long?,
    val syncSuccessRate: Double
)

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    object NoNetwork : SyncStatus()
    object PartialSuccess : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}

// WorkManager Worker for background sync
class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Get SyncService instance (would be injected in real implementation)
            // val syncService = (applicationContext as LedgerPayApp).syncService
            // val result = syncService.syncNow()

            // For demo, just return success
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
