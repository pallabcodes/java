package com.example.ledgerpay.core.network

import retrofit2.http.Body
import retrofit2.http.Header
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
        require(currency == currency.uppercase(Locale.US)) { "Invalid currency code" }
        require(isValidCurrency(currency)) { "Invalid currency code" }
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
        require(status.isNotBlank()) { "Invalid payment status received" }
        require(status in listOf("requires_payment_method", "requires_confirmation", "processing", "succeeded", "failed", "canceled")) {
            "Invalid payment status"
        }
    }
}

data class CreateTransferRequest(
    val amountMinor: Long,
    val currency: String,
    val recipient: String,
    val note: String? = null
) {
    init {
        require(amountMinor > 0) { "Amount must be positive" }
        require(currency.matches(Regex("^[A-Z]{3}$"))) { "Invalid currency code" }
        require(recipient.isNotBlank()) { "Recipient is required" }
        require(note == null || note.length <= 256) { "Transfer note too long" }
    }
}

data class CreateTransferResponse(
    val id: String,
    val status: String
) {
    init {
        require(id.isNotBlank()) { "Transfer ID cannot be blank" }
        require(status.isNotBlank()) { "Transfer status cannot be blank" }
    }
}

data class CreatePaymentRequest(
    val amountMinor: Long,
    val currency: String,
    val payee: String,
    val memo: String? = null
) {
    init {
        require(amountMinor > 0) { "Amount must be positive" }
        require(currency.matches(Regex("^[A-Z]{3}$"))) { "Invalid currency code" }
        require(payee.isNotBlank()) { "Payee is required" }
        require(memo == null || memo.length <= 256) { "Request memo too long" }
    }
}

data class CreatePaymentRequestResponse(
    val id: String,
    val status: String
) {
    init {
        require(id.isNotBlank()) { "Request ID cannot be blank" }
        require(status.isNotBlank()) { "Request status cannot be blank" }
    }
}

data class IntegrityVerificationRequest(
    val nonce: String,
    val rooted: Boolean,
    val signatureValid: Boolean,
    val clientTimestamp: Long = System.currentTimeMillis()
) {
    init {
        require(nonce.isNotBlank()) { "Nonce is required" }
        require(clientTimestamp > 0) { "Client timestamp is required" }
    }
}

data class IntegrityVerificationResponse(
    val accepted: Boolean,
    val token: String? = null,
    val expiresAtEpochMs: Long? = null
) {
    init {
        if (accepted) {
            require(!token.isNullOrBlank()) { "Accepted integrity response must include token" }
            require((expiresAtEpochMs ?: 0L) > 0L) { "Accepted integrity response must include expiry" }
        }
    }
}

// Security: Authentication request/response validation
data class LoginRequest(
    val email: String,
    val password: String
) {
    init {
        // Security: Input validation for login
        require(email.isNotBlank()) { "Email is required" }
        require(email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) { "Invalid email format" }
        require(password.isNotBlank()) { "Password is required" }
        require(password.length >= 8) { "Password must be at least 8 characters" }
    }
}

data class LoginResponse(
    val userId: String,
    val email: String,
    val token: String
) {
    init {
        // Security: Validate response data
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(token.isNotBlank()) { "Token cannot be blank" }
        require(token.length >= 10) { "Token appears invalid" }
    }
}

interface PaymentsApi {
    @POST("/payment_intents")
    suspend fun createIntent(@Body req: CreateIntentRequest): CreateIntentResponse

    @POST("/payment_intents")
    suspend fun createIntent(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body req: CreateIntentRequest
    ): CreateIntentResponse

    @POST("/transfers")
    suspend fun createTransfer(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body req: CreateTransferRequest
    ): CreateTransferResponse

    @POST("/payment_requests")
    suspend fun createPaymentRequest(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body req: CreatePaymentRequest
    ): CreatePaymentRequestResponse

    @POST("/api/auth/login")
    suspend fun login(@Body req: LoginRequest): LoginResponse

    @POST("/api/security/integrity/verify")
    suspend fun verifyIntegrity(@Body req: IntegrityVerificationRequest): IntegrityVerificationResponse
}
