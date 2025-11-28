package com.example.kotlinpay.payments.gateway

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.concurrent.ThreadLocalRandom

/**
 * PayPal Payment Gateway Implementation
 *
 * Handles PayPal-specific payment processing with:
 * - PayPal Payments API
 * - PayPal Express Checkout
 * - Recurring billing support
 * - Global currency support
 * - Buyer/seller protection
 */
@Component
class PayPalGateway {

    private val logger = LoggerFactory.getLogger(PayPalGateway::class.java)

    // Simulate PayPal API configuration
    private val clientId = "paypal_client_simulated"
    private val clientSecret = "paypal_secret_simulated"

    fun processPayment(request: PaymentRequest): PaymentResponse {
        logger.info("Processing payment through PayPal: ${request.transactionId}")

        return try {
            val paypalResponse = simulatePayPalPayment(request)

            PaymentResponse(
                transactionId = request.transactionId,
                success = paypalResponse.success,
                responseCode = paypalResponse.code,
                message = paypalResponse.message,
                gatewayTransactionId = paypalResponse.transactionId,
                processingTimeMs = paypalResponse.processingTime,
                gatewayFees = calculatePayPalFees(request.amount)
            )
        } catch (e: Exception) {
            logger.error("PayPal payment processing failed", e)
            PaymentResponse(
                transactionId = request.transactionId,
                success = false,
                responseCode = "PAYPAL_ERROR",
                message = "PayPal processing error: ${e.message}",
                gatewayTransactionId = null
            )
        }
    }

    fun authorizePayment(request: PaymentRequest): PaymentResponse {
        logger.info("Authorizing payment through PayPal: ${request.transactionId}")

        return try {
            val paypalResponse = simulatePayPalAuthorization(request)

            PaymentResponse(
                transactionId = request.transactionId,
                success = paypalResponse.success,
                responseCode = paypalResponse.code,
                message = paypalResponse.message,
                gatewayTransactionId = paypalResponse.transactionId,
                processingTimeMs = paypalResponse.processingTime
            )
        } catch (e: Exception) {
            logger.error("PayPal authorization failed", e)
            PaymentResponse(
                transactionId = request.transactionId,
                success = false,
                responseCode = "PAYPAL_AUTH_ERROR",
                message = "PayPal authorization error: ${e.message}",
                gatewayTransactionId = null
            )
        }
    }

    fun capturePayment(request: CaptureRequest): PaymentResponse {
        logger.info("Capturing payment through PayPal: ${request.transactionId}")

        return try {
            val paypalResponse = simulatePayPalCapture(request)

            PaymentResponse(
                transactionId = request.transactionId,
                success = paypalResponse.success,
                responseCode = paypalResponse.code,
                message = paypalResponse.message,
                gatewayTransactionId = paypalResponse.transactionId,
                processingTimeMs = paypalResponse.processingTime
            )
        } catch (e: Exception) {
            logger.error("PayPal capture failed", e)
            PaymentResponse(
                transactionId = request.transactionId,
                success = false,
                responseCode = "PAYPAL_CAPTURE_ERROR",
                message = "PayPal capture error: ${e.message}",
                gatewayTransactionId = null
            )
        }
    }

    fun refundPayment(request: RefundRequest): PaymentResponse {
        logger.info("Processing refund through PayPal: ${request.transactionId}")

        return try {
            val paypalResponse = simulatePayPalRefund(request)

            PaymentResponse(
                transactionId = request.transactionId,
                success = paypalResponse.success,
                responseCode = paypalResponse.code,
                message = paypalResponse.message,
                gatewayTransactionId = paypalResponse.transactionId,
                processingTimeMs = paypalResponse.processingTime
            )
        } catch (e: Exception) {
            logger.error("PayPal refund failed", e)
            PaymentResponse(
                transactionId = request.transactionId,
                success = false,
                responseCode = "PAYPAL_REFUND_ERROR",
                message = "PayPal refund error: ${e.message}",
                gatewayTransactionId = null
            )
        }
    }

    fun voidPayment(request: VoidRequest): PaymentResponse {
        logger.info("Voiding payment through PayPal: ${request.transactionId}")

        return try {
            val paypalResponse = simulatePayPalVoid(request)

            PaymentResponse(
                transactionId = request.transactionId,
                success = paypalResponse.success,
                responseCode = paypalResponse.code,
                message = paypalResponse.message,
                gatewayTransactionId = paypalResponse.transactionId,
                processingTimeMs = paypalResponse.processingTime
            )
        } catch (e: Exception) {
            logger.error("PayPal void failed", e)
            PaymentResponse(
                transactionId = request.transactionId,
                success = false,
                responseCode = "PAYPAL_VOID_ERROR",
                message = "PayPal void error: ${e.message}",
                gatewayTransactionId = null
            )
        }
    }

    /**
     * Simulate PayPal API responses for development/testing
     */

    private fun simulatePayPalPayment(request: PaymentRequest): PayPalApiResponse {
        Thread.sleep(ThreadLocalRandom.current().nextLong(200, 800)) // PayPal tends to be slower

        val random = ThreadLocalRandom.current()
        val success = random.nextDouble() > 0.08 // 92% success rate

        return if (success) {
            PayPalApiResponse(
                success = true,
                code = "PAYMENT_SUCCESS",
                message = "Payment completed successfully",
                transactionId = "paypal_txn_${System.currentTimeMillis()}",
                processingTime = random.nextLong(300, 1200)
            )
        } else {
            val errorCodes = listOf("PAYMENT_DENIED", "INSUFFICIENT_FUNDS", "PAYMENT_DECLINED")
            PayPalApiResponse(
                success = false,
                code = errorCodes.random(),
                message = "Payment could not be processed",
                transactionId = null,
                processingTime = random.nextLong(250, 600)
            )
        }
    }

    private fun simulatePayPalAuthorization(request: PaymentRequest): PayPalApiResponse {
        Thread.sleep(ThreadLocalRandom.current().nextLong(100, 400))

        val random = ThreadLocalRandom.current()
        val success = random.nextDouble() > 0.05 // 95% success rate for authorization

        return if (success) {
            PayPalApiResponse(
                success = true,
                code = "AUTHORIZATION_SUCCESS",
                message = "Payment authorized successfully",
                transactionId = "paypal_auth_${System.currentTimeMillis()}",
                processingTime = random.nextLong(150, 500)
            )
        } else {
            PayPalApiResponse(
                success = false,
                code = "AUTHORIZATION_FAILED",
                message = "Authorization declined",
                transactionId = null,
                processingTime = random.nextLong(120, 350)
            )
        }
    }

    private fun simulatePayPalCapture(request: CaptureRequest): PayPalApiResponse {
        Thread.sleep(ThreadLocalRandom.current().nextLong(80, 300))

        val random = ThreadLocalRandom.current()
        val success = random.nextDouble() > 0.02 // 98% success rate for capture

        return if (success) {
            PayPalApiResponse(
                success = true,
                code = "CAPTURE_SUCCESS",
                message = "Payment captured successfully",
                transactionId = "paypal_capture_${System.currentTimeMillis()}",
                processingTime = random.nextLong(100, 400)
            )
        } else {
            PayPalApiResponse(
                success = false,
                code = "CAPTURE_FAILED",
                message = "Capture failed due to processing error",
                transactionId = null,
                processingTime = random.nextLong(80, 250)
            )
        }
    }

    private fun simulatePayPalRefund(request: RefundRequest): PayPalApiResponse {
        Thread.sleep(ThreadLocalRandom.current().nextLong(150, 600))

        val random = ThreadLocalRandom.current()
        val success = random.nextDouble() > 0.03 // 97% success rate for refunds

        return if (success) {
            PayPalApiResponse(
                success = true,
                code = "REFUND_SUCCESS",
                message = "Refund processed successfully",
                transactionId = "paypal_refund_${System.currentTimeMillis()}",
                processingTime = random.nextLong(200, 800)
            )
        } else {
            PayPalApiResponse(
                success = false,
                code = "REFUND_FAILED",
                message = "Refund failed due to insufficient funds",
                transactionId = null,
                processingTime = random.nextLong(180, 500)
            )
        }
    }

    private fun simulatePayPalVoid(request: VoidRequest): PayPalApiResponse {
        Thread.sleep(ThreadLocalRandom.current().nextLong(50, 200))

        val random = ThreadLocalRandom.current()
        val success = random.nextDouble() > 0.015 // 98.5% success rate for voids

        return if (success) {
            PayPalApiResponse(
                success = true,
                code = "VOID_SUCCESS",
                message = "Payment voided successfully",
                transactionId = "paypal_void_${System.currentTimeMillis()}",
                processingTime = random.nextLong(60, 250)
            )
        } else {
            PayPalApiResponse(
                success = false,
                code = "VOID_FAILED",
                message = "Void failed due to processing error",
                transactionId = null,
                processingTime = random.nextLong(50, 180)
            )
        }
    }

    private fun calculatePayPalFees(amount: BigDecimal): BigDecimal {
        // PayPal pricing: 2.9% + 30¢ per transaction (USD)
        val percentageFee = amount.multiply(BigDecimal("0.029"))
        val fixedFee = BigDecimal("0.30")
        return percentageFee.add(fixedFee)
    }

    private data class PayPalApiResponse(
        val success: Boolean,
        val code: String,
        val message: String,
        val transactionId: String?,
        val processingTime: Long
    )
}
