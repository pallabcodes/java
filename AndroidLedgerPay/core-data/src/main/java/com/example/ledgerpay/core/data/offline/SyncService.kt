package com.example.ledgerpay.core.data.offline

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.example.ledgerpay.core.data.prefs.SecureStorage
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionQueue: OfflineTransactionQueue,
    private val secureStorage: SecureStorage,
    private val syncProcessor: OfflineSyncProcessor
) {

    companion object {
        private const val SYNC_WORK_NAME = "offline_transaction_sync"
        private const val SYNC_INTERVAL_MINUTES = 15L
        private const val BATCH_SIZE = 10
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val workManager = WorkManager.getInstance(context)
    private val connectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

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
            if (!isNetworkAvailable()) {
                return@withContext Result.failure(Exception("No network connection"))
            }

            _syncStatus.value = SyncStatus.Syncing
            _syncProgress.value = SyncProgress(0, 0)

            val result = syncProcessor.syncPendingTransactions(BATCH_SIZE) { completed, total ->
                _syncProgress.value = SyncProgress(completed, total)
            }

            result.fold(
                onSuccess = { syncResult ->
                    _syncStatus.value = if (syncResult.failureCount > 0) {
                        SyncStatus.PartialSuccess
                    } else {
                        SyncStatus.Idle
                    }
                    _syncProgress.value = null
                    Result.success(syncResult)
                },
                onFailure = { error ->
                    _syncStatus.value = SyncStatus.Error(error.message ?: "Unknown error")
                    _syncProgress.value = null
                    Result.failure(error)
                }
            )
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }

    private fun registerNetworkCallback() {
        if (networkCallback != null) {
            return
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
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

        connectivityManager.registerDefaultNetworkCallback(callback)
        networkCallback = callback
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

    private suspend fun calculateSuccessRate(): Double {
        val pending = transactionQueue.getTransactionCount()
        val failed = transactionQueue.getFailedTransactionCount()
        val total = pending + failed
        if (total == 0) {
            return 1.0
        }

        return ((total - failed).toDouble() / total.toDouble()).coerceIn(0.0, 1.0)
    }

    fun cancelSync() {
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
        _syncStatus.value = SyncStatus.Idle
        _syncProgress.value = null
    }

    suspend fun cleanup() {
        networkCallback?.let { callback ->
            runCatching { connectivityManager.unregisterNetworkCallback(callback) }
        }
        networkCallback = null
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

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncProcessor: OfflineSyncProcessor
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val MAX_RETRY_ATTEMPTS = 3
    }

    override suspend fun doWork(): Result {
        return syncProcessor.syncPendingTransactions(batchSize = 10).fold(
            onSuccess = { syncResult ->
                if (syncResult.failureCount == 0) {
                    Result.success()
                } else if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            },
            onFailure = {
                if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        )
    }
}
