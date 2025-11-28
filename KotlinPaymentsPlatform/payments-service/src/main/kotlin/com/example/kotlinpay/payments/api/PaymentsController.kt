package com.example.kotlinpay.payments.api

import com.example.kotlinpay.payments.gateway.*
import com.example.kotlinpay.payments.settlement.SettlementService
import com.example.kotlinpay.payments.reconciliation.ReconciliationService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("/api/v1/payments")
class PaymentsController(
    private val paymentGatewayAdapter: PaymentGatewayAdapter,
    private val settlementService: SettlementService,
    private val reconciliationService: ReconciliationService
) {
    private val logger = LoggerFactory.getLogger(PaymentsController::class.java)

    @PostMapping("/process")
    @PreAuthorize("hasRole('USER') or hasRole('MERCHANT') or hasRole('SERVICE') or hasRole('ADMIN')")
    suspend fun processPayment(@RequestBody request: ProcessPaymentRequest): ResponseEntity<PaymentResponseDto> {
        logger.info("Processing payment request: ${request.transactionId}")

        val paymentRequest = PaymentRequest(
            transactionId = request.transactionId,
            amount = BigDecimal.valueOf(request.amount),
            currency = request.currency,
            paymentMethod = request.paymentMethod,
            paymentType = request.paymentType ?: PaymentType.ONE_TIME,
            customerId = request.customerId,
            merchantId = request.merchantId,
            country = request.country,
            metadata = request.metadata
        )

        return try {
            val response = paymentGatewayAdapter.processPayment(paymentRequest).get()

            val responseDto = PaymentResponseDto(
                transactionId = response.transactionId,
                success = response.success,
                responseCode = response.responseCode,
                message = response.message,
                gatewayTransactionId = response.gatewayTransactionId,
                processingTimeMs = response.processingTimeMs,
                gatewayFees = response.gatewayFees
            )

            ResponseEntity.ok(responseDto)

        } catch (e: Exception) {
            logger.error("Payment processing failed", e)
            ResponseEntity.internalServerError().body(
                PaymentResponseDto(
                    transactionId = request.transactionId,
                    success = false,
                    responseCode = "INTERNAL_ERROR",
                    message = "Payment processing failed: ${e.message}",
                    gatewayTransactionId = null
                )
            )
        }
    }

    @PostMapping("/authorize")
    @PreAuthorize("hasRole('USER') or hasRole('MERCHANT') or hasRole('SERVICE') or hasRole('ADMIN')")
    suspend fun authorizePayment(@RequestBody request: AuthorizePaymentRequest): ResponseEntity<PaymentResponseDto> {
        logger.info("Authorizing payment request: ${request.transactionId}")

        val paymentRequest = PaymentRequest(
            transactionId = request.transactionId,
            amount = BigDecimal.valueOf(request.amount),
            currency = request.currency,
            paymentMethod = request.paymentMethod,
            paymentType = request.paymentType ?: PaymentType.ONE_TIME,
            customerId = request.customerId,
            merchantId = request.merchantId,
            country = request.country,
            metadata = request.metadata
        )

        return try {
            val response = paymentGatewayAdapter.authorizePayment(paymentRequest).get()

            val responseDto = PaymentResponseDto(
                transactionId = response.transactionId,
                success = response.success,
                responseCode = response.responseCode,
                message = response.message,
                gatewayTransactionId = response.gatewayTransactionId,
                processingTimeMs = response.processingTimeMs
            )

            ResponseEntity.ok(responseDto)

        } catch (e: Exception) {
            logger.error("Payment authorization failed", e)
            ResponseEntity.internalServerError().body(
                PaymentResponseDto(
                    transactionId = request.transactionId,
                    success = false,
                    responseCode = "INTERNAL_ERROR",
                    message = "Payment authorization failed: ${e.message}",
                    gatewayTransactionId = null
                )
            )
        }
    }

    @PostMapping("/capture")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('SERVICE') or hasRole('ADMIN')")
    suspend fun capturePayment(@RequestBody request: CapturePaymentRequest): ResponseEntity<PaymentResponseDto> {
        logger.info("Capturing payment: ${request.transactionId}")

        val captureRequest = CaptureRequest(
            transactionId = request.transactionId,
            authorizationId = request.authorizationId,
            amount = BigDecimal.valueOf(request.amount),
            currency = request.currency
        )

        return try {
            val response = paymentGatewayAdapter.capturePayment(captureRequest).get()

            val responseDto = PaymentResponseDto(
                transactionId = response.transactionId,
                success = response.success,
                responseCode = response.responseCode,
                message = response.message,
                gatewayTransactionId = response.gatewayTransactionId,
                processingTimeMs = response.processingTimeMs
            )

            ResponseEntity.ok(responseDto)

        } catch (e: Exception) {
            logger.error("Payment capture failed", e)
            ResponseEntity.internalServerError().body(
                PaymentResponseDto(
                    transactionId = request.transactionId,
                    success = false,
                    responseCode = "INTERNAL_ERROR",
                    message = "Payment capture failed: ${e.message}",
                    gatewayTransactionId = null
                )
            )
        }
    }

    @PostMapping("/refund")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('SERVICE') or hasRole('ADMIN')")
    suspend fun refundPayment(@RequestBody request: RefundPaymentRequest): ResponseEntity<PaymentResponseDto> {
        logger.info("Processing refund: ${request.transactionId}")

        val refundRequest = RefundRequest(
            transactionId = request.transactionId,
            originalTransactionId = request.originalTransactionId,
            amount = BigDecimal.valueOf(request.amount),
            currency = request.currency,
            reason = request.reason
        )

        return try {
            val response = paymentGatewayAdapter.refundPayment(refundRequest).get()

            val responseDto = PaymentResponseDto(
                transactionId = response.transactionId,
                success = response.success,
                responseCode = response.responseCode,
                message = response.message,
                gatewayTransactionId = response.gatewayTransactionId,
                processingTimeMs = response.processingTimeMs
            )

            ResponseEntity.ok(responseDto)

        } catch (e: Exception) {
            logger.error("Payment refund failed", e)
            ResponseEntity.internalServerError().body(
                PaymentResponseDto(
                    transactionId = request.transactionId,
                    success = false,
                    responseCode = "INTERNAL_ERROR",
                    message = "Payment refund failed: ${e.message}",
                    gatewayTransactionId = null
                )
            )
        }
    }

    @PostMapping("/void")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('SERVICE') or hasRole('ADMIN')")
    suspend fun voidPayment(@RequestBody request: VoidPaymentRequest): ResponseEntity<PaymentResponseDto> {
        logger.info("Voiding payment: ${request.transactionId}")

        val voidRequest = VoidRequest(
            transactionId = request.transactionId,
            authorizationId = request.authorizationId
        )

        return try {
            val response = paymentGatewayAdapter.voidPayment(voidRequest).get()

            val responseDto = PaymentResponseDto(
                transactionId = response.transactionId,
                success = response.success,
                responseCode = response.responseCode,
                message = response.message,
                gatewayTransactionId = response.gatewayTransactionId,
                processingTimeMs = response.processingTimeMs
            )

            ResponseEntity.ok(responseDto)

        } catch (e: Exception) {
            logger.error("Payment void failed", e)
            ResponseEntity.internalServerError().body(
                PaymentResponseDto(
                    transactionId = request.transactionId,
                    success = false,
                    responseCode = "INTERNAL_ERROR",
                    message = "Payment void failed: ${e.message}",
                    gatewayTransactionId = null
                )
            )
        }
    }

    @PostMapping("/settlement/submit")
    @PreAuthorize("hasRole('SERVICE') or hasRole('ADMIN')")
    fun submitForSettlement(@RequestBody request: SubmitSettlementRequest): ResponseEntity<SettlementResponseDto> {
        logger.info("Submitting transaction for settlement: ${request.transactionId}")

        val settlementTransaction = com.example.kotlinpay.payments.settlement.SettlementTransaction(
            transactionId = request.transactionId,
            gatewayTransactionId = request.gatewayTransactionId,
            amount = BigDecimal.valueOf(request.amount),
            currency = request.currency,
            gateway = request.gateway,
            merchantId = request.merchantId,
            metadata = request.metadata
        )

        return try {
            val result = settlementService.submitForSettlement(settlementTransaction)

            when (result) {
                is com.example.kotlinpay.payments.settlement.SettlementResult.Success -> {
                    ResponseEntity.ok(
                        SettlementResponseDto(
                            success = true,
                            settlementId = result.settlementId,
                            estimatedSettlementDate = result.estimatedSettlementDate,
                            message = "Settlement submitted successfully"
                        )
                    )
                }
                is com.example.kotlinpay.payments.settlement.SettlementResult.Failure -> {
                    ResponseEntity.badRequest().body(
                        SettlementResponseDto(
                            success = false,
                            message = result.error
                        )
                    )
                }
            }

        } catch (e: Exception) {
            logger.error("Settlement submission failed", e)
            ResponseEntity.internalServerError().body(
                SettlementResponseDto(
                    success = false,
                    message = "Settlement submission failed: ${e.message}"
                )
            )
        }
    }

    @GetMapping("/settlement/status/{transactionId}")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('SERVICE') or hasRole('ADMIN')")
    fun getSettlementStatus(@PathVariable transactionId: String): ResponseEntity<SettlementStatusResponseDto?> {
        val status = settlementService.getSettlementStatus(transactionId)

        return if (status != null) {
            val response = SettlementStatusResponseDto(
                transactionId = status.transactionId,
                status = status.status.name,
                settlementDate = status.settlementDate,
                submittedAt = status.submittedAt,
                settledAt = status.settledAt,
                amount = status.amount,
                fees = status.fees,
                gateway = status.gateway
            )
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/gateway/stats")
    @PreAuthorize("hasRole('ADMIN')")
    fun getGatewayStats(): ResponseEntity<GatewayStatsResponseDto> {
        val stats = paymentGatewayAdapter.getGatewayStats()

        val response = GatewayStatsResponseDto(
            overall = GatewayOverallStatsDto(
                totalRequests = stats.overall.totalRequests,
                averageSuccessRate = stats.overall.averageSuccessRate,
                averageProcessingTime = stats.overall.averageProcessingTime
            ),
            gateways = stats.gateways.map { gateway ->
                GatewayStatDto(
                    gateway = gateway.gateway,
                    totalRequests = gateway.totalRequests,
                    successRate = gateway.successRate,
                    averageProcessingTime = gateway.averageProcessingTime,
                    circuitBreakerState = gateway.circuitBreakerState.name
                )
            }
        )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/settlement/report")
    @PreAuthorize("hasRole('ADMIN')")
    fun getSettlementReport(
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate
    ): ResponseEntity<SettlementReportDto> {
        val report = settlementService.generateSettlementReport(startDate, endDate)

        val response = SettlementReportDto(
            periodStart = report.periodStart,
            periodEnd = report.periodEnd,
            totalTransactions = report.totalTransactions,
            totalAmount = report.totalAmount,
            totalFees = report.totalFees,
            netAmount = report.netAmount,
            successfulSettlements = report.successfulSettlements,
            failedSettlements = report.failedSettlements,
            settlementsByStatus = report.settlementsByStatus.mapKeys { it.key.name },
            settlementsByGateway = report.settlementsByGateway,
            generatedAt = report.generatedAt
        )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/reconciliation/report")
    @PreAuthorize("hasRole('ADMIN')")
    fun getReconciliationReport(
        @RequestParam type: String,
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate
    ): ResponseEntity<ReconciliationReportDto> {
        val reconciliationType = com.example.kotlinpay.payments.reconciliation.ReconciliationType.valueOf(type.uppercase())
        val report = reconciliationService.getReconciliationReport(reconciliationType, startDate, endDate)

        val response = ReconciliationReportDto(
            type = report.type.name,
            periodStart = report.periodStart,
            periodEnd = report.periodEnd,
            totalRecords = report.totalRecords,
            successfulRecords = report.successfulRecords,
            discrepancyRecords = report.discrepancyRecords,
            totalAmount = report.totalAmount,
            matchedAmount = report.matchedAmount,
            totalDiscrepancies = report.totalDiscrepancies,
            successRate = report.successRate,
            generatedAt = report.generatedAt
        )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(mapOf(
            "status" to "UP",
            "service" to "payments-service",
            "gateway_adapters" to 4, // Stripe, PayPal, Adyen, Braintree
            "settlement_enabled" to true,
            "reconciliation_enabled" to true,
            "timestamp" to java.time.LocalDateTime.now()
        ))
    }
}

// Request DTOs
data class ProcessPaymentRequest(
    val transactionId: String,
    val amount: Double,
    val currency: String,
    val paymentMethod: PaymentMethod,
    val paymentType: PaymentType? = null,
    val customerId: String,
    val merchantId: String,
    val country: String,
    val metadata: Map<String, String> = emptyMap()
)

data class AuthorizePaymentRequest(
    val transactionId: String,
    val amount: Double,
    val currency: String,
    val paymentMethod: PaymentMethod,
    val paymentType: PaymentType? = null,
    val customerId: String,
    val merchantId: String,
    val country: String,
    val metadata: Map<String, String> = emptyMap()
)

data class CapturePaymentRequest(
    val transactionId: String,
    val authorizationId: String,
    val amount: Double,
    val currency: String
)

data class RefundPaymentRequest(
    val transactionId: String,
    val originalTransactionId: String,
    val amount: Double,
    val currency: String,
    val reason: String = "Customer request"
)

data class VoidPaymentRequest(
    val transactionId: String,
    val authorizationId: String
)

data class SubmitSettlementRequest(
    val transactionId: String,
    val gatewayTransactionId: String,
    val amount: Double,
    val currency: String,
    val gateway: String,
    val merchantId: String,
    val metadata: Map<String, String> = emptyMap()
)

// Response DTOs
data class PaymentResponseDto(
    val transactionId: String,
    val success: Boolean,
    val responseCode: String,
    val message: String,
    val gatewayTransactionId: String? = null,
    val processingTimeMs: Long = 0,
    val gatewayFees: BigDecimal = BigDecimal.ZERO
)

data class SettlementResponseDto(
    val success: Boolean,
    val settlementId: String? = null,
    val estimatedSettlementDate: LocalDate? = null,
    val message: String
)

data class SettlementStatusResponseDto(
    val transactionId: String,
    val status: String,
    val settlementDate: LocalDate,
    val submittedAt: java.time.LocalDateTime,
    val settledAt: java.time.LocalDateTime?,
    val amount: BigDecimal,
    val fees: com.example.kotlinpay.payments.settlement.SettlementFees,
    val gateway: String
)

data class GatewayStatsResponseDto(
    val overall: GatewayOverallStatsDto,
    val gateways: List<GatewayStatDto>
)

data class GatewayOverallStatsDto(
    val totalRequests: Long,
    val averageSuccessRate: Double,
    val averageProcessingTime: Double
)

data class GatewayStatDto(
    val gateway: String,
    val totalRequests: Long,
    val successRate: Double,
    val averageProcessingTime: Double,
    val circuitBreakerState: String
)

data class SettlementReportDto(
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val totalTransactions: Int,
    val totalAmount: BigDecimal,
    val totalFees: BigDecimal,
    val netAmount: BigDecimal,
    val successfulSettlements: Int,
    val failedSettlements: Int,
    val settlementsByStatus: Map<String, Int>,
    val settlementsByGateway: Map<String, Int>,
    val generatedAt: java.time.LocalDateTime
)

data class ReconciliationReportDto(
    val type: String,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val totalRecords: Int,
    val successfulRecords: Int,
    val discrepancyRecords: Int,
    val totalAmount: BigDecimal,
    val matchedAmount: BigDecimal,
    val totalDiscrepancies: Int,
    val successRate: Double,
    val generatedAt: java.time.LocalDateTime
)

// Legacy DTOs (kept for compatibility)
data class CreateIntentRequest(val id: String, val amountMinor: Long, val currency: String)
data class IntentResponse(val id: String, val status: String)

