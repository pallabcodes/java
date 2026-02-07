package com.example.ledgerpay.core.data

import com.example.ledgerpay.core.data.db.PaymentIntentDao
import com.example.ledgerpay.core.data.db.PaymentIntentEntity
import com.example.ledgerpay.core.data.telemetry.Monitoring
import com.example.ledgerpay.core.network.CreateIntentRequest
import com.example.ledgerpay.core.network.PaymentsApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class PaymentsRepository @Inject constructor(
    private val dao: PaymentIntentDao,
    private val api: PaymentsApi,
    private val monitoring: Monitoring,
    private val ioDispatcher: CoroutineDispatcher
) {

    sealed class Result<out T> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error(val exception: Exception) : Result<Nothing>()
    }

    suspend fun createIntent(amountMinor: Long, currency: String): Result<String> = withContext(ioDispatcher) {
        monitoring.measurePerformance("create_payment_intent") {
            try {
                // Security: Validate inputs before API call
                validatePaymentInputs(amountMinor, currency)

                monitoring.logPaymentEvent("intent_creation_started", amount = amountMinor, currency = currency)

                val res = api.createIntent(CreateIntentRequest(amountMinor, currency))

                // Security: Validate response before storing
                validatePaymentResponse(res)

                val entity = PaymentIntentEntity(res.id, amountMinor, currency, status = res.status)
                dao.upsert(entity)

                monitoring.logPaymentEvent("intent_creation_success", res.id, amountMinor, currency)
                monitoring.logBusinessMetric("payment_intent_created", 1)

                Result.Success(res.id)
            } catch (e: Exception) {
                monitoring.logError(e, "Failed to create payment intent", userVisible = true)
                monitoring.logBusinessMetric("payment_intent_failed", 1)
                Result.Error(mapException(e))
            }
        }
    }

    suspend fun listRecent(): Result<List<PaymentIntentEntity>> = withContext(ioDispatcher) {
        monitoring.measurePerformance("list_recent_payments") {
            try {
                val entities = dao.list()
                monitoring.logBusinessMetric("recent_payments_loaded", entities.size)
                Result.Success(entities)
            } catch (e: Exception) {
                monitoring.logError(e, "Failed to load recent payments")
                Result.Error(mapException(e))
            }
        }
    }

    private fun validatePaymentInputs(amountMinor: Long, currency: String) {
        require(amountMinor > 0) { "Payment amount must be positive" }
        require(amountMinor <= 1_000_000_00L) { "Payment amount exceeds maximum allowed" }
        require(currency.isNotBlank()) { "Currency is required" }
        require(currency.matches(Regex("^[A-Z]{3}$"))) { "Invalid currency format" }
    }

    private fun validatePaymentResponse(response: com.example.ledgerpay.core.network.CreateIntentResponse) {
        require(response.id.isNotBlank()) { "Invalid payment ID received" }
        require(response.status.isNotBlank()) { "Invalid payment status received" }
    }

    private fun mapException(e: Exception): Exception {
        return when (e) {
            is HttpException -> when (e.code()) {
                400 -> IllegalArgumentException("Invalid payment request")
                401 -> SecurityException("Authentication required")
                403 -> SecurityException("Access denied")
                429 -> RuntimeException("Rate limit exceeded")
                in 500..599 -> RuntimeException("Server error")
                else -> RuntimeException("HTTP error: ${e.code()}")
            }
            is SocketTimeoutException -> RuntimeException("Request timeout")
            is UnknownHostException -> RuntimeException("Network unavailable")
            is IOException -> RuntimeException("Network error")
            else -> e
        }
    }
}
