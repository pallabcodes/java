package com.example.kotlinpay.ledger.domain

import com.example.kotlinpay.shared.AccountId
import com.example.kotlinpay.shared.Direction
import com.example.kotlinpay.shared.LedgerEntry
import com.example.kotlinpay.shared.Money

data class Journal(val id: String, val entries: List<LedgerEntry>)

object DoubleEntryValidator {
    fun validate(journal: Journal) {
        val currency = journal.entries.map { it.amount.currency }.toSet()
        require(currency.size == 1) { "mixed currency journal" }
        val sum = journal.entries.sumOf { if (it.direction == Direction.DEBIT) it.amount.amountMinor else -it.amount.amountMinor }
        require(sum == 0L) { "imbalanced journal" }
    }
}

data class Posting(val accountId: AccountId, val amount: Money, val direction: Direction, val description: String)


