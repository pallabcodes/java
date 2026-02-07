package com.example.ledgerpay.core.data.offline

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.room.*
import androidx.room.Room.databaseBuilder
import com.example.ledgerpay.core.data.prefs.SecureStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.security.KeyStore
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
    @ApplicationContext private val context: Context,
    private val secureStorage: SecureStorage
) {

    companion object {
        private const val DATABASE_NAME = "offline_transactions.db"
        private const val MAX_QUEUE_SIZE = 1000
        private const val ENCRYPTION_KEY_ALIAS = "offline_transaction_key"
        private const val GCM_TAG_LENGTH = 128
        private const val GCM_IV_LENGTH = 12
        private const val IV_KEY_AMOUNT = "amount"
        private const val IV_KEY_RECIPIENT = "recipient"
        private const val IV_KEY_DESCRIPTION = "description"
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
        return transactionDao.observePendingTransactions().map { encrypted ->
            encrypted.mapNotNull { runCatching { decryptTransaction(it) }.getOrNull() }
        }
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

    suspend fun incrementRetryCount(transactionId: String, error: String): Boolean {
        return try {
            transactionDao.incrementRetryCount(transactionId, error) > 0
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getFailedTransactionCount(): Int {
        return try {
            transactionDao.getFailedTransactionCount()
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
            // Use a unique nonce per encrypted field to avoid AES-GCM nonce reuse.
            val amountField = encryptField(transaction.amount.toString())
            val recipientField = encryptField(transaction.recipient)
            val descriptionField = transaction.description?.let { encryptField(it) }

            EncryptedOfflineTransaction(
                id = transaction.id,
                type = transaction.type,
                encryptedAmount = amountField.ciphertext,
                encryptedRecipient = recipientField.ciphertext,
                encryptedDescription = descriptionField?.ciphertext,
                iv = buildIvBundle(amountField.iv, recipientField.iv, descriptionField?.iv),
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
            val ivBundle = parseIvBundle(encrypted.iv)
            val decryptedAmount = decryptField(encrypted.encryptedAmount, ivBundle[IV_KEY_AMOUNT] ?: encrypted.iv)
            val decryptedRecipient = decryptField(encrypted.encryptedRecipient, ivBundle[IV_KEY_RECIPIENT] ?: encrypted.iv)
            val decryptedDescription = encrypted.encryptedDescription?.let {
                decryptField(it, ivBundle[IV_KEY_DESCRIPTION] ?: encrypted.iv)
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

    private fun encryptField(plainText: String): EncryptedField {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val ivBytes = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(ivBytes)
        val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, ivBytes)
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, parameterSpec)
        val encrypted = cipher.doFinal(plainText.toByteArray())

        return EncryptedField(
            ciphertext = android.util.Base64.encodeToString(encrypted, android.util.Base64.NO_WRAP),
            iv = android.util.Base64.encodeToString(ivBytes, android.util.Base64.NO_WRAP)
        )
    }

    private fun decryptField(ciphertext: String, iv: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val ivBytes = android.util.Base64.decode(iv, android.util.Base64.NO_WRAP)
        val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, ivBytes)
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey, parameterSpec)
        val encryptedBytes = android.util.Base64.decode(ciphertext, android.util.Base64.NO_WRAP)
        return String(cipher.doFinal(encryptedBytes))
    }

    private fun buildIvBundle(amountIv: String, recipientIv: String, descriptionIv: String?): String {
        return JSONObject()
            .put(IV_KEY_AMOUNT, amountIv)
            .put(IV_KEY_RECIPIENT, recipientIv)
            .put(IV_KEY_DESCRIPTION, descriptionIv ?: JSONObject.NULL)
            .toString()
    }

    private fun parseIvBundle(rawIv: String): Map<String, String> {
        return try {
            val json = JSONObject(rawIv)
            buildMap {
                json.optString(IV_KEY_AMOUNT).takeIf { it.isNotBlank() }?.let { put(IV_KEY_AMOUNT, it) }
                json.optString(IV_KEY_RECIPIENT).takeIf { it.isNotBlank() }?.let { put(IV_KEY_RECIPIENT, it) }
                json.optString(IV_KEY_DESCRIPTION).takeIf { it.isNotBlank() && it != "null" }?.let { put(IV_KEY_DESCRIPTION, it) }
            }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun getOrCreateEncryptionKey(): SecretKey {
        return runCatching { getOrCreateKeystoreKey() }
            .getOrElse {
                // Backward-compat fallback for environments where keystore setup fails.
                val storedKey = secureStorage.getEncryptionKey(ENCRYPTION_KEY_ALIAS)
                if (storedKey != null) {
                    return SecretKeySpec(storedKey, "AES")
                }

                val keyGenerator = KeyGenerator.getInstance("AES")
                keyGenerator.init(256)
                val newKey = keyGenerator.generateKey()
                secureStorage.storeEncryptionKey(ENCRYPTION_KEY_ALIAS, newKey.encoded)
                newKey
            }
    }

    private fun getOrCreateKeystoreKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existing = keyStore.getEntry(ENCRYPTION_KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existing != null) {
            return existing.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        val spec = KeyGenParameterSpec.Builder(
            ENCRYPTION_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    suspend fun cleanup() {
        try {
            database.close()
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
}

private data class EncryptedField(
    val ciphertext: String,
    val iv: String
)

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
