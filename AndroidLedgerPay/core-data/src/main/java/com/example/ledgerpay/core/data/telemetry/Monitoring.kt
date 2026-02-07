package com.example.ledgerpay.core.data.telemetry

interface Monitoring {
    fun log(message: String)

    fun logUserAction(event: String, attributes: Map<String, Any?> = emptyMap())

    fun logSecurityEvent(event: String, attributes: Map<String, Any?> = emptyMap())

    fun logPaymentEvent(
        event: String,
        paymentId: String? = null,
        amount: Long? = null,
        currency: String? = null
    )

    fun logBusinessMetric(name: String, value: Number)

    fun logError(error: Throwable, context: String? = null, userVisible: Boolean = false)

    suspend fun <T> measurePerformance(operation: String, block: suspend () -> T): T
}
