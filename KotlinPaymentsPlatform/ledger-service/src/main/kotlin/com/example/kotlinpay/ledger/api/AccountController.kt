package com.example.kotlinpay.ledger.api

import com.example.kotlinpay.shared.AccountId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class AccountController {
    @GetMapping("/accounts/{id}/balance")
    fun balance(@PathVariable id: String): ResponseEntity<Map<String, Any>> =
        ResponseEntity.ok(mapOf("accountId" to AccountId(id).value, "balanceMinor" to 0L, "currency" to "USD"))
}


