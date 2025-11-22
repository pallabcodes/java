package com.example.ledgerpay.core.network

import retrofit2.http.Body
import retrofit2.http.POST
import java.util.Currency
import java.util.Locale

// Security: Input validation for payment amounts
data class CreateIntentRequest(
    val amountMinor: Long,
    val currency: String
) {
    init {
        // Security: Validate amount is positive and reasonable
        require(amountMinor > 0) { "Amount must be positive" }
        require(amountMinor <= 1_000_000_00) { "Amount too large" } // Max $10,000

        // Security: Validate currency code
        require(currency.isNotBlank()) { "Currency is required" }
        require(currency.length == 3) { "Currency must be 3 characters" }
        require(isValidCurrency(currency.uppercase(Locale.US))) { "Invalid currency code" }
    }

    private fun isValidCurrency(currencyCode: String): Boolean {
        return try {
            Currency.getInstance(currencyCode)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}

// Security: Response validation
data class CreateIntentResponse(
    val id: String,
    val status: String
) {
    init {
        // Security: Validate response data
        require(id.isNotBlank()) { "Payment ID cannot be blank" }
        require(id.matches(Regex("^[a-zA-Z0-9_-]+\$"))) { "Invalid payment ID format" }
        require(status in listOf("requires_payment_method", "requires_confirmation", "processing", "succeeded", "failed", "canceled")) {
            "Invalid payment status"
        }
    }
}

interface PaymentsApi {
    @POST("/payment_intents")
    suspend fun createIntent(@Body req: CreateIntentRequest): CreateIntentResponse
}
