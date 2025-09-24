package com.example.kotlinpay.payments.api

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class CreateIntentRequest(val id: String, val amountMinor: Long, val currency: String)
data class IntentResponse(val id: String, val status: String)

@RestController
class PaymentsController {
    @PostMapping("/payment_intents")
    fun create(
        @RequestHeader("Idempotency-Key") idem: String,
        @RequestBody req: CreateIntentRequest
    ): ResponseEntity<IntentResponse> {
        // store intent or return existing by idempotency key
        return ResponseEntity.ok(IntentResponse(req.id, "requires_confirmation"))
    }

    @PostMapping("/payment_intents/{id}/confirm")
    fun confirm(@PathVariable id: String): ResponseEntity<IntentResponse> {
        // mark as succeeded and write outbox event
        return ResponseEntity.ok(IntentResponse(id, "succeeded"))
    }
}


