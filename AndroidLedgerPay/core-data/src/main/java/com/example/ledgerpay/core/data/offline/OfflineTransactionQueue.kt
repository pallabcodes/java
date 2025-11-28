package com.example.ledgerpay.core.data.offline

import android.content.Context
import androidx.room.*
import androidx.room.Room.databaseBuilder
import com.example.ledgerpay.core.data.prefs.SecureStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineTransactionQueue @Inject constructor(
    private val context: Context,
    private val secureStorage: SecureStorage
) {

    companion object {
        private const val DATABASE_NAME = "offline_transactions.db"
        private const val MAX_QUEUE_SIZE = 1000
        private const val ENCRYPTION_KEY_ALIAS = "offline_transaction_key"
        private const val GCM_TAG_LENGTH = 128
    }

    private val database: OfflineTransactionDatabase by lazy {
        databaseBuilder(
            context,
            OfflineTransactionDatabase::class.java,
            DATABASE_NAME
        ).build()
    }

    private val transactionDao by lazy { database.transactionDao() }

    // Thread-safe queue management
    private val queueMutex = Mutex()
    private val _queueStatus = MutableStateFlow<QueueStatus>(QueueStatus.Idle)
    val queueStatus = _queueStatus.asStateFlow()

    // Encryption setup
    private val encryptionKey: SecretKey by lazy { getOrCreateEncryptionKey() }

    suspend fun enqueueTransaction(transaction: OfflineTransaction): Result<Unit> {
        return queueMutex.withLock {
            try {
                // Check queue size limits
                val currentSize = transactionDao.getTransactionCount()
                if (currentSize >= MAX_QUEUE_SIZE) {
                    return Result.failure(IllegalStateException("Queue is full"))
                }

                // Encrypt sensitive data
                val encryptedTransaction = encryptTransaction(transaction)

                // Store in database
                transactionDao.insertTransaction(encryptedTransaction)

                _queueStatus.value = QueueStatus.Active(currentSize + 1)

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun dequeueTransaction(): OfflineTransaction? {
        return queueMutex.withLock {
            try {
                val encryptedTransaction = transactionDao.getNextTransaction()
                encryptedTransaction?.let { decryptTransaction(it) }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun peekTransaction(): OfflineTransaction? {
        return try {
            val encryptedTransaction = transactionDao.peekNextTransaction()
            encryptedTransaction?.let { decryptTransaction(it) }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun removeTransaction(transactionId: String): Boolean {
        return try {
            val deletedCount = transactionDao.deleteTransaction(transactionId)
            if (deletedCount > 0) {
                updateQueueStatus()
            }
            deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAllPendingTransactions(): List<OfflineTransaction> {
        return try {
            transactionDao.getAllPendingTransactions()
                .mapNotNull { runCatching { decryptTransaction(it) }.getOrNull() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTransactionCount(): Int {
        return try {
            transactionDao.getTransactionCount()
        } catch (e: Exception) {
            0
        }
    }

    fun observePendingTransactions(): Flow<List<OfflineTransaction>> {
        return transactionDao.observePendingTransactions()
    }

    suspend fun updateTransactionStatus(transactionId: String, status: TransactionStatus): Boolean {
        return try {
            val updatedCount = transactionDao.updateTransactionStatus(transactionId, status)
            if (updatedCount > 0) {
                updateQueueStatus()
            }
            updatedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    suspend fun clearProcessedTransactions(): Int {
        return try {
            val clearedCount = transactionDao.deleteProcessedTransactions()
            if (clearedCount > 0) {
                updateQueueStatus()
            }
            clearedCount
        } catch (e: Exception) {
            0
        }
    }

    suspend fun handleConflict(transactionId: String, resolution: ConflictResolution): Result<Unit> {
        return try {
            when (resolution) {
                ConflictResolution.KEEP_LOCAL -> {
                    // Mark as resolved locally
                    transactionDao.updateTransactionStatus(transactionId, TransactionStatus.CONFLICT_RESOLVED)
                }
                ConflictResolution.USE_SERVER -> {
                    // Remove local transaction
                    transactionDao.deleteTransaction(transactionId)
                }
                ConflictResolution.MERGE -> {
                    // Mark for manual review
                    transactionDao.updateTransactionStatus(transactionId, TransactionStatus.NEEDS_REVIEW)
                }
            }
            updateQueueStatus()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateQueueStatus() {
        val count = getTransactionCount()
        _queueStatus.value = if (count > 0) QueueStatus.Active(count) else QueueStatus.Idle
    }

    private fun encryptTransaction(transaction: OfflineTransaction): EncryptedOfflineTransaction {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = ByteArray(12) // GCM IV length
            SecureRandom().nextBytes(iv)
            val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)

            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, parameterSpec)

            // Encrypt sensitive fields
            val encryptedAmount = cipher.doFinal(transaction.amount.toString().toByteArray())
            val encryptedRecipient = cipher.doFinal(transaction.recipient.toByteArray())
            val encryptedDescription = transaction.description?.let {
                cipher.doFinal(it.toByteArray())
            }

            EncryptedOfflineTransaction(
                id = transaction.id,
                type = transaction.type,
                encryptedAmount = android.util.Base64.encodeToString(encryptedAmount, android.util.Base64.NO_WRAP),
                encryptedRecipient = android.util.Base64.encodeToString(encryptedRecipient, android.util.Base64.NO_WRAP),
                encryptedDescription = encryptedDescription?.let {
                    android.util.Base64.encodeToString(it, android.util.Base64.NO_WRAP)
                },
                iv = android.util.Base64.encodeToString(iv, android.util.Base64.NO_WRAP),
                timestamp = transaction.timestamp,
                status = transaction.status,
                retryCount = transaction.retryCount,
                lastError = transaction.lastError
            )
        } catch (e: Exception) {
            throw RuntimeException("Failed to encrypt transaction", e)
        }
    }

    private fun decryptTransaction(encrypted: EncryptedOfflineTransaction): OfflineTransaction {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = android.util.Base64.decode(encrypted.iv, android.util.Base64.NO_WRAP)
            val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)

            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, parameterSpec)

            // Decrypt sensitive fields
            val decryptedAmount = String(cipher.doFinal(android.util.Base64.decode(encrypted.encryptedAmount, android.util.Base64.NO_WRAP)))
            val decryptedRecipient = String(cipher.doFinal(android.util.Base64.decode(encrypted.encryptedRecipient, android.util.Base64.NO_WRAP)))
            val decryptedDescription = encrypted.encryptedDescription?.let {
                String(cipher.doFinal(android.util.Base64.decode(it, android.util.Base64.NO_WRAP)))
            }

            OfflineTransaction(
                id = encrypted.id,
                type = encrypted.type,
                amount = decryptedAmount.toDoubleOrNull() ?: 0.0,
                recipient = decryptedRecipient,
                description = decryptedDescription,
                timestamp = encrypted.timestamp,
                status = encrypted.status,
                retryCount = encrypted.retryCount,
                lastError = encrypted.lastError
            )
        } catch (e: Exception) {
            throw RuntimeException("Failed to decrypt transaction", e)
        }
    }

    private fun getOrCreateEncryptionKey(): SecretKey {
        // Try to get existing key from secure storage
        val storedKey = secureStorage.getEncryptionKey(ENCRYPTION_KEY_ALIAS)
        if (storedKey != null) {
            return SecretKeySpec(storedKey, "AES")
        }

        // Generate new key
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        val newKey = keyGenerator.generateKey()

        // Store the key securely
        secureStorage.storeEncryptionKey(ENCRYPTION_KEY_ALIAS, newKey.encoded)

        return newKey
    }

    suspend fun cleanup() {
        try {
            database.close()
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
}

// Data classes
data class OfflineTransaction(
    val id: String = UUID.randomUUID().toString(),
    val type: TransactionType,
    val amount: Double,
    val recipient: String,
    val description: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val status: TransactionStatus = TransactionStatus.PENDING,
    val retryCount: Int = 0,
    val lastError: String? = null
)

enum class TransactionType {
    PAYMENT,
    TRANSFER,
    REQUEST
}

enum class TransactionStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CONFLICT_RESOLVED,
    NEEDS_REVIEW
}

enum class ConflictResolution {
    KEEP_LOCAL,
    USE_SERVER,
    MERGE
}

sealed class QueueStatus {
    object Idle : QueueStatus()
    data class Active(val pendingCount: Int) : QueueStatus()
}
