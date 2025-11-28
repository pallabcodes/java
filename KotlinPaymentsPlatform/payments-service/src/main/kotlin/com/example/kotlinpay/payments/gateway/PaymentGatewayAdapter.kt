package com.example.kotlinpay.payments.gateway

import com.example.kotlinpay.shared.compliance.AuditLogger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * Payment Gateway Adapter Service
 *
 * Provides unified interface for multiple payment gateways:
 * - Stripe
 * - PayPal
 * - Adyen
 * - Braintree
 * - Square
 * - Intelligent routing based on transaction characteristics
 * - Fallback mechanisms and circuit breakers
 * - Real-time monitoring and alerting
 */
@Service
class PaymentGatewayAdapter(
    private val auditLogger: AuditLogger,
    private val stripeGateway: StripeGateway,
    private val payPalGateway: PayPalGateway,
    private val adyenGateway: AdyenGateway,
    private val braintreeGateway: BraintreeGateway
) {
    private val logger = LoggerFactory.getLogger(PaymentGatewayAdapter::class.java)

    // Gateway performance metrics
    private val gatewayMetrics = ConcurrentHashMap<String, GatewayMetrics>()

    // Circuit breaker states
    private val circuitBreakers = ConcurrentHashMap<String, CircuitBreaker>()

    // Gateway routing rules
    private val routingRules = ConcurrentHashMap<String, RoutingRule>()

    init {
        initializeGateways()
        initializeRoutingRules()
    }

    /**
     * Process payment through optimal gateway
     */
    fun processPayment(request: PaymentRequest): CompletableFuture<PaymentResponse> {
        return CompletableFuture.supplyAsync {
            val startTime = System.nanoTime()

            try {
                // Select optimal gateway
                val selectedGateway = selectGateway(request)

                logger.info("Processing payment ${request.transactionId} through ${selectedGateway.name}")

                // Execute payment
                val response = when (selectedGateway) {
                    GatewayType.STRIPE -> stripeGateway.processPayment(request)
                    GatewayType.PAYPAL -> payPalGateway.processPayment(request)
                    GatewayType.ADYEN -> adyenGateway.processPayment(request)
                    GatewayType.BRAINTREE -> braintreeGateway.processPayment(request)
                }

                val processingTime = (System.nanoTime() - startTime) / 1_000_000 // Convert to milliseconds

                // Update metrics
                updateGatewayMetrics(selectedGateway.name, response.success, processingTime)

                // Audit log
                auditLogger.logSecurityEvent(
                    event = if (response.success) "PAYMENT_SUCCESS" else "PAYMENT_FAILED",
                    severity = if (response.success) "INFO" else "WARNING",
                    details = mapOf(
                        "transaction_id" to request.transactionId,
                        "gateway" to selectedGateway.name,
                        "amount" to request.amount.toString(),
                        "currency" to request.currency,
                        "processing_time_ms" to processingTime.toString(),
                        "response_code" to response.responseCode
                    )
                )

                response

            } catch (e: Exception) {
                logger.error("Payment processing failed for transaction: ${request.transactionId}", e)

                auditLogger.logSecurityEvent(
                    event = "PAYMENT_PROCESSING_ERROR",
                    severity = "CRITICAL",
                    details = mapOf(
                        "transaction_id" to request.transactionId,
                        "error" to e.message.toString()
                    )
                )

                PaymentResponse(
                    transactionId = request.transactionId,
                    success = false,
                    responseCode = "INTERNAL_ERROR",
                    message = "Payment processing failed: ${e.message}",
                    gatewayTransactionId = null,
                    processingTimeMs = (System.nanoTime() - startTime) / 1_000_000
                )
            }
        }
    }

    /**
     * Authorize payment (hold funds)
     */
    fun authorizePayment(request: PaymentRequest): CompletableFuture<PaymentResponse> {
        return CompletableFuture.supplyAsync {
            try {
                val gateway = selectGateway(request)

                logger.info("Authorizing payment ${request.transactionId} through ${gateway.name}")

                val response = when (gateway) {
                    GatewayType.STRIPE -> stripeGateway.authorizePayment(request)
                    GatewayType.PAYPAL -> payPalGateway.authorizePayment(request)
                    GatewayType.ADYEN -> adyenGateway.authorizePayment(request)
                    GatewayType.BRAINTREE -> braintreeGateway.authorizePayment(request)
                }

                auditLogger.logSecurityEvent(
                    event = if (response.success) "PAYMENT_AUTHORIZED" else "PAYMENT_AUTH_FAILED",
                    severity = if (response.success) "INFO" else "WARNING",
                    details = mapOf(
                        "transaction_id" to request.transactionId,
                        "gateway" to gateway.name,
                        "amount" to request.amount.toString()
                    )
                )

                response

            } catch (e: Exception) {
                logger.error("Payment authorization failed for transaction: ${request.transactionId}", e)
                PaymentResponse(
                    transactionId = request.transactionId,
                    success = false,
                    responseCode = "AUTH_ERROR",
                    message = "Authorization failed: ${e.message}",
                    gatewayTransactionId = null
                )
            }
        }
    }

    /**
     * Capture authorized payment
     */
    fun capturePayment(request: CaptureRequest): CompletableFuture<PaymentResponse> {
        return CompletableFuture.supplyAsync {
            try {
                val gateway = GatewayType.STRIPE // Assume Stripe for capture, in production determine from auth

                logger.info("Capturing payment ${request.transactionId}")

                val response = when (gateway) {
                    GatewayType.STRIPE -> stripeGateway.capturePayment(request)
                    GatewayType.PAYPAL -> payPalGateway.capturePayment(request)
                    GatewayType.ADYEN -> adyenGateway.capturePayment(request)
                    GatewayType.BRAINTREE -> braintreeGateway.capturePayment(request)
                }

                auditLogger.logSecurityEvent(
                    event = if (response.success) "PAYMENT_CAPTURED" else "PAYMENT_CAPTURE_FAILED",
                    severity = if (response.success) "INFO" else "WARNING",
                    details = mapOf(
                        "transaction_id" to request.transactionId,
                        "amount" to request.amount.toString()
                    )
                )

                response

            } catch (e: Exception) {
                logger.error("Payment capture failed for transaction: ${request.transactionId}", e)
                PaymentResponse(
                    transactionId = request.transactionId,
                    success = false,
                    responseCode = "CAPTURE_ERROR",
                    message = "Capture failed: ${e.message}",
                    gatewayTransactionId = null
                )
            }
        }
    }

    /**
     * Refund payment
     */
    fun refundPayment(request: RefundRequest): CompletableFuture<PaymentResponse> {
        return CompletableFuture.supplyAsync {
            try {
                val gateway = GatewayType.STRIPE // In production, determine from original transaction

                logger.info("Processing refund for transaction ${request.transactionId}")

                val response = when (gateway) {
                    GatewayType.STRIPE -> stripeGateway.refundPayment(request)
                    GatewayType.PAYPAL -> payPalGateway.refundPayment(request)
                    GatewayType.ADYEN -> adyenGateway.refundPayment(request)
                    GatewayType.BRAINTREE -> braintreeGateway.refundPayment(request)
                }

                auditLogger.logSecurityEvent(
                    event = if (response.success) "PAYMENT_REFUNDED" else "PAYMENT_REFUND_FAILED",
                    severity = if (response.success) "INFO" else "WARNING",
                    details = mapOf(
                        "transaction_id" to request.transactionId,
                        "refund_amount" to request.amount.toString()
                    )
                )

                response

            } catch (e: Exception) {
                logger.error("Payment refund failed for transaction: ${request.transactionId}", e)
                PaymentResponse(
                    transactionId = request.transactionId,
                    success = false,
                    responseCode = "REFUND_ERROR",
                    message = "Refund failed: ${e.message}",
                    gatewayTransactionId = null
                )
            }
        }
    }

    /**
     * Void payment (cancel authorization)
     */
    fun voidPayment(request: VoidRequest): CompletableFuture<PaymentResponse> {
        return CompletableFuture.supplyAsync {
            try {
                val gateway = GatewayType.STRIPE // In production, determine from original transaction

                logger.info("Voiding payment ${request.transactionId}")

                val response = when (gateway) {
                    GatewayType.STRIPE -> stripeGateway.voidPayment(request)
                    GatewayType.PAYPAL -> payPalGateway.voidPayment(request)
                    GatewayType.ADYEN -> adyenGateway.voidPayment(request)
                    GatewayType.BRAINTREE -> braintreeGateway.voidPayment(request)
                }

                auditLogger.logSecurityEvent(
                    event = "PAYMENT_VOIDED",
                    severity = "INFO",
                    details = mapOf("transaction_id" to request.transactionId)
                )

                response

            } catch (e: Exception) {
                logger.error("Payment void failed for transaction: ${request.transactionId}", e)
                PaymentResponse(
                    transactionId = request.transactionId,
                    success = false,
                    responseCode = "VOID_ERROR",
                    message = "Void failed: ${e.message}",
                    gatewayTransactionId = null
                )
            }
        }
    }

    /**
     * Intelligent gateway selection
     */
    private fun selectGateway(request: PaymentRequest): GatewayType {
        // Check circuit breakers first
        val availableGateways = GatewayType.values().filter { gateway ->
            val breaker = circuitBreakers[gateway.name]
            breaker?.state != CircuitBreakerState.OPEN
        }

        if (availableGateways.isEmpty()) {
            logger.warn("All gateways are down, using fallback")
            return GatewayType.STRIPE // Fallback to Stripe
        }

        // Apply routing rules based on transaction characteristics
        val applicableRules = routingRules.values.filter { rule ->
            rule.condition(request)
        }.sortedByDescending { it.priority }

        val selectedGateway = applicableRules.firstOrNull()?.gateway ?: availableGateways.first()

        // Check if gateway is in half-open state
        val breaker = circuitBreakers[selectedGateway.name]
        if (breaker?.state == CircuitBreakerState.HALF_OPEN) {
            // Allow limited traffic for testing
            breaker.testRequests++
            if (breaker.testRequests >= breaker.testThreshold) {
                breaker.state = CircuitBreakerState.CLOSED
                logger.info("Circuit breaker for ${selectedGateway.name} closed")
            }
        }

        return selectedGateway
    }

    /**
     * Update gateway metrics and circuit breaker state
     */
    private fun updateGatewayMetrics(gatewayName: String, success: Boolean, processingTime: Long) {
        val metrics = gatewayMetrics.getOrPut(gatewayName) { GatewayMetrics(gatewayName) }

        metrics.totalRequests++
        if (success) {
            metrics.successfulRequests++
        } else {
            metrics.failedRequests++
        }

        metrics.averageProcessingTime = ((metrics.averageProcessingTime * (metrics.totalRequests - 1)) + processingTime) / metrics.totalRequests

        // Update circuit breaker
        updateCircuitBreaker(gatewayName, success)
    }

    /**
     * Circuit breaker logic
     */
    private fun updateCircuitBreaker(gatewayName: String, success: Boolean) {
        val breaker = circuitBreakers.getOrPut(gatewayName) {
            CircuitBreaker(gatewayName, failureThreshold = 5, recoveryTimeout = 60000) // 1 minute
        }

        when (breaker.state) {
            CircuitBreakerState.CLOSED -> {
                if (!success) {
                    breaker.failureCount++
                    if (breaker.failureCount >= breaker.failureThreshold) {
                        breaker.state = CircuitBreakerState.OPEN
                        breaker.lastFailureTime = System.currentTimeMillis()
                        logger.warn("Circuit breaker for $gatewayName opened due to failures")
                    }
                } else {
                    breaker.failureCount = 0 // Reset on success
                }
            }
            CircuitBreakerState.OPEN -> {
                if (System.currentTimeMillis() - breaker.lastFailureTime > breaker.recoveryTimeout) {
                    breaker.state = CircuitBreakerState.HALF_OPEN
                    breaker.testRequests = 0
                    logger.info("Circuit breaker for $gatewayName entering half-open state")
                }
            }
            CircuitBreakerState.HALF_OPEN -> {
                // State management handled in selectGateway
            }
        }
    }

    /**
     * Initialize gateway configurations
     */
    private fun initializeGateways() {
        GatewayType.values().forEach { gateway ->
            gatewayMetrics[gateway.name] = GatewayMetrics(gateway.name)
            circuitBreakers[gateway.name] = CircuitBreaker(gateway.name)
        }
    }

    /**
     * Initialize intelligent routing rules
     */
    private fun initializeRoutingRules() {
        // Rule 1: High-value transactions use Stripe (better fraud detection)
        routingRules["high_value_stripe"] = RoutingRule(
            name = "high_value_stripe",
            gateway = GatewayType.STRIPE,
            priority = 10,
            condition = { request -> request.amount >= BigDecimal("500") }
        )

        // Rule 2: International transactions use Adyen (global coverage)
        routingRules["international_adyen"] = RoutingRule(
            name = "international_adyen",
            gateway = GatewayType.ADYEN,
            priority = 9,
            condition = { request -> request.country != "US" }
        )

        // Rule 3: Recurring payments use PayPal (subscription handling)
        routingRules["recurring_paypal"] = RoutingRule(
            name = "recurring_paypal",
            gateway = GatewayType.PAYPAL,
            priority = 8,
            condition = { request -> request.paymentType == PaymentType.RECURRING }
        )

        // Rule 4: US domestic transactions use Stripe (cost effective)
        routingRules["us_domestic_stripe"] = RoutingRule(
            name = "us_domestic_stripe",
            gateway = GatewayType.STRIPE,
            priority = 1,
            condition = { request -> request.country == "US" }
        )
    }

    /**
     * Get gateway performance statistics
     */
    fun getGatewayStats(): GatewayStatsResponse {
        val gatewayStats = gatewayMetrics.values.map { metrics ->
            GatewayStat(
                gateway = metrics.gateway,
                totalRequests = metrics.totalRequests,
                successRate = if (metrics.totalRequests > 0) metrics.successfulRequests.toDouble() / metrics.totalRequests else 0.0,
                averageProcessingTime = metrics.averageProcessingTime,
                circuitBreakerState = circuitBreakers[metrics.gateway]?.state ?: CircuitBreakerState.CLOSED
            )
        }

        val overallStats = GatewayOverallStats(
            totalRequests = gatewayStats.sumOf { it.totalRequests },
            averageSuccessRate = gatewayStats.map { it.successRate }.average(),
            averageProcessingTime = gatewayStats.map { it.averageProcessingTime }.average()
        )

        return GatewayStatsResponse(overallStats, gatewayStats)
    }

    /**
     * Manually open/close circuit breaker for maintenance
     */
    fun setCircuitBreakerState(gatewayName: String, state: CircuitBreakerState) {
        val breaker = circuitBreakers[gatewayName]
        if (breaker != null) {
            breaker.state = state
            logger.info("Circuit breaker for $gatewayName manually set to $state")
        }
    }
}

/**
 * Data classes for payment gateway adapter
 */

data class PaymentRequest(
    val transactionId: String,
    val amount: BigDecimal,
    val currency: String,
    val paymentMethod: PaymentMethod,
    val paymentType: PaymentType = PaymentType.ONE_TIME,
    val customerId: String,
    val merchantId: String,
    val country: String,
    val metadata: Map<String, String> = emptyMap()
)

data class CaptureRequest(
    val transactionId: String,
    val authorizationId: String,
    val amount: BigDecimal,
    val currency: String
)

data class RefundRequest(
    val transactionId: String,
    val originalTransactionId: String,
    val amount: BigDecimal,
    val currency: String,
    val reason: String = "Customer request"
)

data class VoidRequest(
    val transactionId: String,
    val authorizationId: String
)

data class PaymentResponse(
    val transactionId: String,
    val success: Boolean,
    val responseCode: String,
    val message: String,
    val gatewayTransactionId: String? = null,
    val processingTimeMs: Long = 0,
    val gatewayFees: BigDecimal = BigDecimal.ZERO
)

data class PaymentMethod(
    val type: String, // "credit_card", "debit_card", "paypal", etc.
    val token: String, // Tokenized payment method
    val lastFour: String? = null,
    val expiryMonth: Int? = null,
    val expiryYear: Int? = null
)

enum class PaymentType {
    ONE_TIME,
    RECURRING,
    SUBSCRIPTION
}

enum class GatewayType {
    STRIPE,
    PAYPAL,
    ADYEN,
    BRAINTREE
}

data class GatewayMetrics(
    val gateway: String,
    var totalRequests: Long = 0,
    var successfulRequests: Long = 0,
    var failedRequests: Long = 0,
    var averageProcessingTime: Double = 0.0,
    var lastUpdated: LocalDateTime = LocalDateTime.now()
)

data class CircuitBreaker(
    val gatewayName: String,
    val failureThreshold: Int = 5,
    val recoveryTimeout: Long = 60000, // 1 minute
    var state: CircuitBreakerState = CircuitBreakerState.CLOSED,
    var failureCount: Int = 0,
    var lastFailureTime: Long = 0,
    var testRequests: Int = 0,
    val testThreshold: Int = 3
)

enum class CircuitBreakerState {
    CLOSED,    // Normal operation
    OPEN,      // Failing, requests rejected
    HALF_OPEN  // Testing recovery
}

data class RoutingRule(
    val name: String,
    val gateway: GatewayType,
    val priority: Int,
    val condition: (PaymentRequest) -> Boolean
)

data class GatewayStatsResponse(
    val overall: GatewayOverallStats,
    val gateways: List<GatewayStat>
)

data class GatewayOverallStats(
    val totalRequests: Long,
    val averageSuccessRate: Double,
    val averageProcessingTime: Double
)

data class GatewayStat(
    val gateway: String,
    val totalRequests: Long,
    val successRate: Double,
    val averageProcessingTime: Double,
    val circuitBreakerState: CircuitBreakerState
)
