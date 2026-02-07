package com.example.ledgerpay.feature.payments.offline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ledgerpay.core.data.offline.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class OfflinePaymentHandler @Inject constructor(
    private val transactionQueue: OfflineTransactionQueue,
    private val syncService: SyncService
) : ViewModel() {

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState = _paymentState.asStateFlow()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState = _syncState.asStateFlow()

    private val _transactionStatus = MutableStateFlow<TransactionStatus?>(null)
    val transactionStatus = _transactionStatus.asStateFlow()

    init {
        observeSyncStatus()
        observeQueueStatus()
    }

    fun processOfflinePayment(
        amount: Double,
        recipient: String,
        description: String? = null
    ): Result<String> {
        return try {
            if (amount <= 0) {
                return Result.failure(IllegalArgumentException("Amount must be positive"))
            }

            if (recipient.isBlank()) {
                return Result.failure(IllegalArgumentException("Recipient is required"))
            }

            val transaction = OfflineTransaction(
                type = TransactionType.PAYMENT,
                amount = amount,
                recipient = recipient,
                description = description
            )

            viewModelScope.launch {
                _paymentState.value = PaymentState.Processing

                transactionQueue.enqueueTransaction(transaction).fold(
                    onSuccess = {
                        _paymentState.value = PaymentState.Success(transaction.id)
                        _transactionStatus.value = TransactionStatus.Queued
                    },
                    onFailure = { error ->
                        _paymentState.value = PaymentState.Error(error.message ?: "Unknown error")
                    }
                )
            }

            Result.success(transaction.id)

        } catch (e: Exception) {
            _paymentState.value = PaymentState.Error(e.message ?: "Unknown error")
            Result.failure(e)
        }
    }

    fun processOfflineTransfer(
        amount: Double,
        recipient: String,
        description: String? = null
    ): Result<String> {
        return try {
            if (amount <= 0) {
                return Result.failure(IllegalArgumentException("Amount must be positive"))
            }

            if (recipient.isBlank()) {
                return Result.failure(IllegalArgumentException("Recipient is required"))
            }

            val transaction = OfflineTransaction(
                type = TransactionType.TRANSFER,
                amount = amount,
                recipient = recipient,
                description = description
            )

            viewModelScope.launch {
                _paymentState.value = PaymentState.Processing

                transactionQueue.enqueueTransaction(transaction).fold(
                    onSuccess = {
                        _paymentState.value = PaymentState.Success(transaction.id)
                        _transactionStatus.value = TransactionStatus.Queued
                    },
                    onFailure = { error ->
                        _paymentState.value = PaymentState.Error(error.message ?: "Unknown error")
                    }
                )
            }

            Result.success(transaction.id)

        } catch (e: Exception) {
            _paymentState.value = PaymentState.Error(e.message ?: "Unknown error")
            Result.failure(e)
        }
    }

    fun syncTransactions() {
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing

            syncService.syncNow().fold(
                onSuccess = { result ->
                    _syncState.value = SyncState.Success(
                        syncedCount = result.successCount,
                        failedCount = result.failureCount,
                        cleanedCount = result.cleanedCount
                    )

                    // Update transaction statuses for failed transactions
                    result.failedTransactionIds.forEach { transactionId ->
                        viewModelScope.launch {
                            transactionQueue.updateTransactionStatus(
                                transactionId,
                                com.example.ledgerpay.core.data.offline.TransactionStatus.FAILED
                            )
                        }
                    }
                },
                onFailure = { error ->
                    _syncState.value = SyncState.Error(error.message ?: "Sync failed")
                }
            )
        }
    }

    fun getPendingTransactions(): Flow<List<OfflineTransaction>> {
        return transactionQueue.observePendingTransactions()
    }

    suspend fun getTransactionDetails(transactionId: String): OfflineTransaction? {
        return transactionQueue.getAllPendingTransactions()
            .find { it.id == transactionId }
    }

    fun resolveConflict(transactionId: String, resolution: ConflictResolution) {
        viewModelScope.launch {
            transactionQueue.handleConflict(transactionId, resolution).fold(
                onSuccess = {
                    _transactionStatus.value = when (resolution) {
                        ConflictResolution.KEEP_LOCAL -> TransactionStatus.ConflictResolved
                        ConflictResolution.USE_SERVER -> TransactionStatus.Removed
                        ConflictResolution.MERGE -> TransactionStatus.NeedsReview
                    }
                },
                onFailure = { error ->
                    _transactionStatus.value = TransactionStatus.Error(error.message ?: "Conflict resolution failed")
                }
            )
        }
    }

    suspend fun getSyncStatistics(): SyncStatistics {
        return syncService.getSyncStatistics()
    }

    fun cancelSync() {
        syncService.cancelSync()
        _syncState.value = SyncState.Idle
    }

    private fun observeSyncStatus() {
        viewModelScope.launch {
            syncService.syncStatus.collect { status ->
                _syncState.value = when (status) {
                    is com.example.ledgerpay.core.data.offline.SyncStatus.Idle -> SyncState.Idle
                    is com.example.ledgerpay.core.data.offline.SyncStatus.Syncing -> SyncState.Syncing
                    is com.example.ledgerpay.core.data.offline.SyncStatus.NoNetwork -> SyncState.NoNetwork
                    is com.example.ledgerpay.core.data.offline.SyncStatus.PartialSuccess -> SyncState.PartialSuccess
                    is com.example.ledgerpay.core.data.offline.SyncStatus.Error -> SyncState.Error(status.message)
                }
            }
        }
    }

    private fun observeQueueStatus() {
        viewModelScope.launch {
            transactionQueue.queueStatus.collect { status ->
                when (status) {
                    is com.example.ledgerpay.core.data.offline.QueueStatus.Idle ->
                        _transactionStatus.value = TransactionStatus.Idle
                    is com.example.ledgerpay.core.data.offline.QueueStatus.Active ->
                        _transactionStatus.value = TransactionStatus.Queued
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            syncService.cleanup()
            transactionQueue.cleanup()
        }
    }
}

// UI-friendly state classes
sealed class PaymentState {
    object Idle : PaymentState()
    object Processing : PaymentState()
    data class Success(val transactionId: String) : PaymentState()
    data class Error(val message: String) : PaymentState()
}

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    object NoNetwork : SyncState()
    object PartialSuccess : SyncState()
    data class Success(
        val syncedCount: Int,
        val failedCount: Int,
        val cleanedCount: Int
    ) : SyncState()
    data class Error(val message: String) : SyncState()
}

sealed class TransactionStatus {
    object Idle : TransactionStatus()
    object Queued : TransactionStatus()
    object Processing : TransactionStatus()
    object Completed : TransactionStatus()
    object Failed : TransactionStatus()
    object ConflictResolved : TransactionStatus()
    object NeedsReview : TransactionStatus()
    object Removed : TransactionStatus()
    data class Error(val message: String) : TransactionStatus()
}
