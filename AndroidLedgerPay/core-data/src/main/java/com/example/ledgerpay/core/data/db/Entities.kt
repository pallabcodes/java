package com.example.ledgerpay.core.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_intent")
data class PaymentIntentEntity(
    @PrimaryKey val id: String,
    val amountMinor: Long,
    val currency: String,
    val status: String
)
