package com.example.kotlinpay.shared

data class Money(val amountMinor: Long, val currency: String)

data class AccountId(val value: String)

data class LedgerEntry(
    val id: String,
    val accountId: AccountId,
    val amount: Money,
    val direction: Direction,
    val journalId: String,
    val description: String
)

enum class Direction { CREDIT, DEBIT }


