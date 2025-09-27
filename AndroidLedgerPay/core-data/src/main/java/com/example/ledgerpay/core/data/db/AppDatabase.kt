package com.example.ledgerpay.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PaymentIntentEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun paymentIntentDao(): PaymentIntentDao
}
