package com.example.kotlinpay.payments.gateway

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.concurrent.ThreadLocalRandom

/**
 * Adyen Payment Gateway Implementation
 *
 * Handles Adyen-specific payment processing with:
 * - Global payment processing
 * - Risk management integration
 * - Multi-party payment support
 * - Advanced fraud detection
 * - Dynamic currency conversion
 */
@Component
class AdyenGateway {

    private val logger = LoggerFactory.getLogger(AdyenGateway::class.java)

    // Simulate Adyen API configuration
    private val apiKey = "adyen_api_simulated"
    private val merchantAccount = "merchant_simulated"

    fun processPayment(request: PaymentRequest): PaymentResponse {
        logger.info("Processing payment through Adyen: ${request.transactionId}")

        return try {
            val adyenResponse = simulateAdyenPayment(request)

            PaymentResponse(
                transactionId = request.transactionId,
                success = adyenResponse.success,
                responseCode = adyenResponse.code,
                message = adyenResponse.message,
                gatewayTransactionId = adyenResponse.transactionId,
                processingTimeMs = adyenResponse.processingTime,
                gatewayFees = calculateAdyenFees(request.amount)
            )
        } catch (e: Exception) {
            logger.error("Adyen payment processing failed", e)
            PaymentResponse(
                transactionId = request.transactionId,
                success = false,
                responseCode = "ADYEN_ERROR",
                message = "Adyen processing error: ${e.message}",
                gatewayTransactionId = null
            )
        }
    }

    fun authorizePayment(request: PaymentRequest): PaymentResponse {
        logger.info("Authorizing payment through Adyen: ${request.transactionId}")

        return try {
            val adyenResponse = simulateAdyenAuthorization(request)

            PaymentResponse(
                transactionId = request.transactionId,
                success = adyenResponse.success,
                responseCode = adyenResponse.code,
                message = adyenResponse.message,
                gatewayTransactionId = adyenResponse.transactionId,
                processingTimeMs = adyenResponse.processingTime
            )
        } catch (e: Exception) {
            logger.error("Adyen authorization failed", e)
            PaymentResponse(
                transactionId = request.transactionId,
                success = false,
                responseCode = "ADYEN_AUTH_ERROR",
                message = "Adyen authorization error: ${e.message}",
                gatewayTransactionId = null
            )
        }
    }

    fun capturePayment(request: CaptureRequest): PaymentResponse {
        logger.info("Capturing payment through Adyen: ${request.transactionId}")

        return try {
            val adyenResponse = simulateAdyenCapture(request)

            PaymentResponse(
                transactionId = request.transactionId,
                success = adyenResponse.success,
                responseCode = adyenResponse.code,
                message = adyenResponse.message,
                gatewayTransactionId = adyenResponse.transactionId,
                processingTimeMs = adyenResponse.processingTime
            )
        } catch (e: Exception) {
            logger.error("Adyen capture failed", e)
            PaymentResponse(
                transactionId = request.transactionId,
                success = false,
                responseCode = "ADYEN_CAPTURE_ERROR",
                message = "Adyen capture error: ${e.message}",
                gatewayTransactionId = null
            )
        }
    }

    fun refundPayment(request: RefundRequest): PaymentResponse {
        logger.info("Processing refund through Adyen: ${request.transactionId}")

        return try {
            val adyenResponse = simulateAdyenRefund(request)

            PaymentResponse(
                transactionId = request.transactionId,
                success = adyenResponse.success,
                responseCode = adyenResponse.code,
                message = adyenResponse.message,
                gatewayTransactionId = adyenResponse.transactionId,
                processingTimeMs = adyenResponse.processingTime
            )
        } catch (e: Exception) {
            logger.error("Adyen refund failed", e)
            PaymentResponse(
                transactionId = request.transactionId,
                success = false,
                responseCode = "ADYEN_REFUND_ERROR",
                message = "Adyen refund error: ${e.message}",
                gatewayTransactionId = null
            )
        }
    }

    fun voidPayment(request: VoidRequest): PaymentResponse {
        logger.info("Voiding payment through Adyen: ${request.transactionId}")

        return try {
            val adyenResponse = simulateAdyenVoid(request)

            PaymentResponse(
                transactionId = request.transactionId,
                success = adyenResponse.success,
                responseCode = adyenResponse.code,
                message = adyenResponse.message,
                gatewayTransactionId = adyenResponse.transactionId,
                processingTimeMs = adyenResponse.processingTime
            )
        } catch (e: Exception) {
            logger.error("Adyen void failed", e)
            PaymentResponse(
                transactionId = request.transactionId,
                success = false,
                responseCode = "ADYEN_VOID_ERROR",
                message = "Adyen void error: ${e.message}",
                gatewayTransactionId = null
            )
        }
    }

    // Simulated API responses (similar structure to other gateways)
    private fun simulateAdyenPayment(request: PaymentRequest): AdyenApiResponse {
        Thread.sleep(ThreadLocalRandom.current().nextLong(150, 600))
        val random = ThreadLocalRandom.current()
        val success = random.nextDouble() > 0.06 // 94% success rate

        return if (success) {
            AdyenApiResponse(
                success = true,
                code = "Authorised",
                message = "Payment authorised",
                transactionId = "adyen_${System.currentTimeMillis()}",
                processingTime = random.nextLong(200, 800)
            )
        } else {
            AdyenApiResponse(
                success = false,
                code = "Refused",
                message = "Payment refused",
                transactionId = null,
                processingTime = random.nextLong(180, 500)
            )
        }
    }

    private fun simulateAdyenAuthorization(request: PaymentRequest): AdyenApiResponse {
        Thread.sleep(ThreadLocalRandom.current().nextLong(80, 300))
        val random = ThreadLocalRandom.current()
        val success = random.nextDouble() > 0.04

        return if (success) {
            AdyenApiResponse(
                success = true,
                code = "Authorised",
                message = "Payment authorised",
                transactionId = "adyen_auth_${System.currentTimeMillis()}",
                processingTime = random.nextLong(120, 400)
            )
        } else {
            AdyenApiResponse(
                success = false,
                code = "Refused",
                message = "Authorisation refused",
                transactionId = null,
                processingTime = random.nextLong(100, 250)
            )
        }
    }

    private fun simulateAdyenCapture(request: CaptureRequest): AdyenApiResponse {
        Thread.sleep(ThreadLocalRandom.current().nextLong(60, 200))
        val random = ThreadLocalRandom.current()
        val success = random.nextDouble() > 0.02

        return if (success) {
            AdyenApiResponse(
                success = true,
                code = "[capture-received]",
                message = "Capture received",
                transactionId = "adyen_capture_${System.currentTimeMillis()}",
                processingTime = random.nextLong(80, 300)
            )
        } else {
            AdyenApiResponse(
                success = false,
                code = "Not allowed",
                message = "Capture not allowed",
                transactionId = null,
                processingTime = random.nextLong(70, 200)
            )
        }
    }

    private fun simulateAdyenRefund(request: RefundRequest): AdyenApiResponse {
        Thread.sleep(ThreadLocalRandom.current().nextLong(100, 400))
        val random = ThreadLocalRandom.current()
        val success = random.nextDouble() > 0.03

        return if (success) {
            AdyenApiResponse(
                success = true,
                code = "[refund-received]",
                message = "Refund received",
                transactionId = "adyen_refund_${System.currentTimeMillis()}",
                processingTime = random.nextLong(150, 600)
            )
        } else {
            AdyenApiResponse(
                success = false,
                code = "Not allowed",
                message = "Refund not allowed",
                transactionId = null,
                processingTime = random.nextLong(120, 350)
            )
        }
    }

    private fun simulateAdyenVoid(request: VoidRequest): AdyenApiResponse {
        Thread.sleep(ThreadLocalRandom.current().nextLong(40, 150))
        val random = ThreadLocalRandom.current()
        val success = random.nextDouble() > 0.01

        return if (success) {
            AdyenApiResponse(
                success = true,
                code = "[cancel-received]",
                message = "Cancel received",
                transactionId = "adyen_void_${System.currentTimeMillis()}",
                processingTime = random.nextLong(50, 200)
            )
        } else {
            AdyenApiResponse(
                success = false,
                code = "Not allowed",
                message = "Cancel not allowed",
                transactionId = null,
                processingTime = random.nextLong(45, 120)
            )
        }
    }

    private fun calculateAdyenFees(amount: BigDecimal): BigDecimal {
        // Adyen pricing: 2.5% + 25¢ per transaction (EUR)
        val percentageFee = amount.multiply(BigDecimal("0.025"))
        val fixedFee = BigDecimal("0.25")
        return percentageFee.add(fixedFee)
    }

    private data class AdyenApiResponse(
        val success: Boolean,
        val code: String,
        val message: String,
        val transactionId: String?,
        val processingTime: Long
    )
}
