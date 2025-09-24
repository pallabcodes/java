package com.example.kotlinpay.ledger.api

import com.example.kotlinpay.ledger.domain.DoubleEntryValidator
import com.example.kotlinpay.ledger.domain.Journal
import com.example.kotlinpay.shared.LedgerEntry
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

data class JournalRequest(val id: String, val entries: List<EntryReq>)
data class EntryReq(
    val id: String,
    val journalId: String,
    val accountId: String,
    val amountMinor: Long,
    val currency: String,
    val direction: String,
    val description: String
)

@RestController
class JournalController {
    @PostMapping("/journals/validate")
    fun validate(@RequestBody req: JournalRequest): ResponseEntity<Map<String, String>> {
        val entries = req.entries.map {
            LedgerEntry(
                id = it.id,
                accountId = com.example.kotlinpay.shared.AccountId(it.accountId),
                amount = com.example.kotlinpay.shared.Money(it.amountMinor, it.currency),
                direction = com.example.kotlinpay.shared.Direction.valueOf(it.direction),
                journalId = it.journalId,
                description = it.description
            )
        }
        DoubleEntryValidator.validate(Journal(req.id, entries))
        return ResponseEntity.ok(mapOf("status" to "ok"))
    }
}


