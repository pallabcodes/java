package com.example.ledgerpay.core.data.offline

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Database(
    entities = [EncryptedOfflineTransaction::class],
    version = 1,
    exportSchema = true
)
abstract class OfflineTransactionDatabase : RoomDatabase() {
    abstract fun transactionDao(): OfflineTransactionDao
}

@Entity(tableName = "offline_transactions")
data class EncryptedOfflineTransaction(
    @PrimaryKey
    val id: String,
    val type: TransactionType,
    val encryptedAmount: String,
    val encryptedRecipient: String,
    val encryptedDescription: String?,
    val iv: String, // Initialization vector for encryption
    val timestamp: Long,
    val status: TransactionStatus,
    val retryCount: Int = 0,
    val lastError: String? = null
)

@Dao
interface OfflineTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: EncryptedOfflineTransaction)

    @Query("SELECT * FROM offline_transactions WHERE status IN ('PENDING', 'PROCESSING') ORDER BY timestamp ASC LIMIT 1")
    suspend fun getNextTransaction(): EncryptedOfflineTransaction?

    @Query("SELECT * FROM offline_transactions WHERE status IN ('PENDING', 'PROCESSING') ORDER BY timestamp ASC LIMIT 1")
    suspend fun peekNextTransaction(): EncryptedOfflineTransaction?

    @Query("SELECT * FROM offline_transactions WHERE status IN ('PENDING', 'PROCESSING') ORDER BY timestamp ASC")
    suspend fun getAllPendingTransactions(): List<EncryptedOfflineTransaction>

    @Query("SELECT * FROM offline_transactions WHERE status IN ('PENDING', 'PROCESSING') ORDER BY timestamp ASC")
    fun observePendingTransactions(): Flow<List<EncryptedOfflineTransaction>>

    @Query("SELECT COUNT(*) FROM offline_transactions WHERE status IN ('PENDING', 'PROCESSING')")
    suspend fun getTransactionCount(): Int

    @Query("UPDATE offline_transactions SET status = :status WHERE id = :transactionId")
    suspend fun updateTransactionStatus(transactionId: String, status: TransactionStatus): Int

    @Query("DELETE FROM offline_transactions WHERE id = :transactionId")
    suspend fun deleteTransaction(transactionId: String): Int

    @Query("DELETE FROM offline_transactions WHERE status = 'COMPLETED'")
    suspend fun deleteProcessedTransactions(): Int

    @Query("DELETE FROM offline_transactions WHERE status = 'FAILED' AND retryCount >= 3")
    suspend fun deleteFailedTransactions(): Int

    @Query("UPDATE offline_transactions SET retryCount = retryCount + 1, lastError = :error WHERE id = :transactionId")
    suspend fun incrementRetryCount(transactionId: String, error: String): Int

    @Query("SELECT * FROM offline_transactions WHERE status = 'NEEDS_REVIEW' ORDER BY timestamp DESC")
    suspend fun getTransactionsNeedingReview(): List<EncryptedOfflineTransaction>

    @Query("SELECT COUNT(*) FROM offline_transactions WHERE status = 'FAILED'")
    suspend fun getFailedTransactionCount(): Int

    @Query("SELECT AVG(retryCount) FROM offline_transactions WHERE status = 'COMPLETED'")
    suspend fun getAverageRetryCount(): Float?

    // Cleanup old completed transactions (older than 30 days)
    @Query("DELETE FROM offline_transactions WHERE status = 'COMPLETED' AND timestamp < :cutoffTime")
    suspend fun deleteOldCompletedTransactions(cutoffTime: Long): Int
}
