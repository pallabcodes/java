package com.example.kotlinpay.ledger.api

import com.example.kotlinpay.shared.AccountId
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class AccountController {
    @GetMapping("/accounts/{id}/balance")
    @PreAuthorize("hasRole('USER') or hasRole('SERVICE') or hasRole('ADMIN')")
    fun balance(@PathVariable id: String): ResponseEntity<Map<String, Any>> =
        ResponseEntity.ok(mapOf("accountId" to AccountId(id).value, "balanceMinor" to 0L, "currency" to "USD"))
}


