package com.example.ledgerpay.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PaymentIntentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(e: PaymentIntentEntity)

    @Query("select * from payment_intent where id = :id")
    suspend fun find(id: String): PaymentIntentEntity?

    @Query("select * from payment_intent order by rowid desc limit 20")
    suspend fun list(): List<PaymentIntentEntity>
}
