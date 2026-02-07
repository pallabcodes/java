package com.example.ledgerpay.telemetry

import com.example.ledgerpay.core.data.telemetry.Monitoring
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppMonitoring @Inject constructor() : Monitoring {

    override fun log(message: String) {
        Timber.i(message)
    }

    override fun logUserAction(event: String, attributes: Map<String, Any?>) {
        Timber.i("user_action=%s attrs=%s", event, attributes)
    }

    override fun logSecurityEvent(event: String, attributes: Map<String, Any?>) {
        Timber.w("security_event=%s attrs=%s", event, attributes)
    }

    override fun logPaymentEvent(
        event: String,
        paymentId: String?,
        amount: Long?,
        currency: String?
    ) {
        Timber.i(
            "payment_event=%s payment_id=%s amount=%s currency=%s",
            event,
            paymentId,
            amount,
            currency
        )
    }

    override fun logBusinessMetric(name: String, value: Number) {
        Timber.i("metric=%s value=%s", name, value)
    }

    override fun logError(error: Throwable, context: String?, userVisible: Boolean) {
        Timber.e(error, "context=%s user_visible=%s", context ?: "n/a", userVisible)
    }

    override suspend fun <T> measurePerformance(operation: String, block: suspend () -> T): T {
        val startNs = System.nanoTime()
        val result = block()
        val elapsedMs = (System.nanoTime() - startNs) / 1_000_000
        Timber.d("op=%s duration_ms=%s", operation, elapsedMs)
        return result
    }
}
