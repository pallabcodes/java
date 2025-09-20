package com.netflix.springframework.demo.controller;

import com.netflix.springframework.demo.dto.PaymentRequest;
import com.netflix.springframework.demo.entity.PaymentEntity;
import com.netflix.springframework.demo.service.StripePaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * PaymentController - REST Controller for Payment Management
 * 
 * This controller demonstrates Netflix production-grade payment API implementation:
 * 1. RESTful API design with proper HTTP methods and status codes
 * 2. Comprehensive input validation with Bean Validation
 * 3. Robust error handling and exception management
 * 4. Security considerations for payment operations
 * 5. Monitoring and logging for payment requests
 * 6. API documentation and response standardization
 * 
 * For C/C++ engineers:
 * - REST controllers are like API endpoints in C++ web frameworks
 * - @RestController is like marking a class as an API handler
 * - @RequestMapping is like URL routing in C++ web frameworks
 * - @Valid is like input validation in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/v1/payments")
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private static final String CONTROLLER_NAME = "PaymentController";
    
    private final StripePaymentService paymentService;
    
    /**
     * Constructor with dependency injection
     * 
     * @param paymentService Payment service
     */
    @Autowired
    public PaymentController(StripePaymentService paymentService) {
        this.paymentService = paymentService;
        logger.info("{} initialized", CONTROLLER_NAME);
    }
    
    /**
     * Create payment intent
     * 
     * @param paymentRequest Payment request data
     * @param userId User ID making the payment
     * @return Created payment entity
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentEntity>> createPaymentIntent(
            @Valid @RequestBody PaymentRequest paymentRequest,
            @RequestHeader("X-User-ID") @NotNull @Positive Long userId) {
        
        logger.info("{} - Creating payment intent for user: {}, amount: {}", 
                   CONTROLLER_NAME, userId, paymentRequest.getFormattedAmount());
        
        try {
            PaymentEntity payment = paymentService.createPaymentIntent(paymentRequest, userId);
            
            logger.info("{} - Payment intent created successfully. Payment ID: {}", 
                       CONTROLLER_NAME, payment.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(payment, "Payment intent created successfully"));
            
        } catch (IllegalArgumentException e) {
            logger.warn("{} - Invalid payment request for user: {}, error: {}", 
                       CONTROLLER_NAME, userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid payment request: " + e.getMessage()));
            
        } catch (Exception e) {
            logger.error("{} - Error creating payment intent for user: {}", 
                        CONTROLLER_NAME, userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create payment intent"));
        }
    }
    
    /**
     * Confirm payment intent
     * 
     * @param paymentId Payment ID
     * @return Updated payment entity
     */
    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<ApiResponse<PaymentEntity>> confirmPaymentIntent(
            @PathVariable @NotNull @Positive Long paymentId) {
        
        logger.info("{} - Confirming payment intent for payment ID: {}", 
                   CONTROLLER_NAME, paymentId);
        
        try {
            PaymentEntity payment = paymentService.confirmPaymentIntent(paymentId);
            
            logger.info("{} - Payment intent confirmed successfully. Payment ID: {}, Status: {}", 
                       CONTROLLER_NAME, paymentId, payment.getStatus());
            
            return ResponseEntity.ok(ApiResponse.success(payment, "Payment intent confirmed successfully"));
            
        } catch (IllegalArgumentException e) {
            logger.warn("{} - Invalid payment ID: {}, error: {}", 
                       CONTROLLER_NAME, paymentId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid payment ID: " + e.getMessage()));
            
        } catch (Exception e) {
            logger.error("{} - Error confirming payment intent for payment ID: {}", 
                        CONTROLLER_NAME, paymentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to confirm payment intent"));
        }
    }
    
    /**
     * Cancel payment intent
     * 
     * @param paymentId Payment ID
     * @return Updated payment entity
     */
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<ApiResponse<PaymentEntity>> cancelPaymentIntent(
            @PathVariable @NotNull @Positive Long paymentId) {
        
        logger.info("{} - Cancelling payment intent for payment ID: {}", 
                   CONTROLLER_NAME, paymentId);
        
        try {
            PaymentEntity payment = paymentService.cancelPaymentIntent(paymentId);
            
            logger.info("{} - Payment intent cancelled successfully. Payment ID: {}, Status: {}", 
                       CONTROLLER_NAME, paymentId, payment.getStatus());
            
            return ResponseEntity.ok(ApiResponse.success(payment, "Payment intent cancelled successfully"));
            
        } catch (IllegalArgumentException e) {
            logger.warn("{} - Invalid payment ID: {}, error: {}", 
                       CONTROLLER_NAME, paymentId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid payment ID: " + e.getMessage()));
            
        } catch (Exception e) {
            logger.error("{} - Error cancelling payment intent for payment ID: {}", 
                        CONTROLLER_NAME, paymentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to cancel payment intent"));
        }
    }
    
    /**
     * Refund payment
     * 
     * @param paymentId Payment ID
     * @param refundRequest Refund request data
     * @return Updated payment entity
     */
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<ApiResponse<PaymentEntity>> refundPayment(
            @PathVariable @NotNull @Positive Long paymentId,
            @RequestBody RefundRequest refundRequest) {
        
        logger.info("{} - Refunding payment for payment ID: {}, amount: {}", 
                   CONTROLLER_NAME, paymentId, refundRequest.getAmount());
        
        try {
            PaymentEntity payment = paymentService.refundPayment(
                    paymentId, 
                    refundRequest.getAmount(), 
                    refundRequest.getReason());
            
            logger.info("{} - Payment refunded successfully. Payment ID: {}, Refund Amount: {}", 
                       CONTROLLER_NAME, paymentId, refundRequest.getAmount());
            
            return ResponseEntity.ok(ApiResponse.success(payment, "Payment refunded successfully"));
            
        } catch (IllegalArgumentException e) {
            logger.warn("{} - Invalid refund request for payment ID: {}, error: {}", 
                       CONTROLLER_NAME, paymentId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid refund request: " + e.getMessage()));
            
        } catch (Exception e) {
            logger.error("{} - Error refunding payment for payment ID: {}", 
                        CONTROLLER_NAME, paymentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to refund payment"));
        }
    }
    
    /**
     * Get payment by ID
     * 
     * @param paymentId Payment ID
     * @return Payment entity if found
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentEntity>> getPaymentById(
            @PathVariable @NotNull @Positive Long paymentId) {
        
        logger.debug("{} - Getting payment by ID: {}", CONTROLLER_NAME, paymentId);
        
        try {
            Optional<PaymentEntity> paymentOpt = paymentService.getPaymentById(paymentId);
            
            if (paymentOpt.isPresent()) {
                logger.debug("{} - Payment found for ID: {}", CONTROLLER_NAME, paymentId);
                return ResponseEntity.ok(ApiResponse.success(paymentOpt.get(), "Payment retrieved successfully"));
            } else {
                logger.warn("{} - Payment not found for ID: {}", CONTROLLER_NAME, paymentId);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("{} - Error getting payment by ID: {}", CONTROLLER_NAME, paymentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve payment"));
        }
    }
    
    /**
     * Get payment by Stripe payment intent ID
     * 
     * @param stripePaymentIntentId Stripe payment intent ID
     * @return Payment entity if found
     */
    @GetMapping("/stripe/{stripePaymentIntentId}")
    public ResponseEntity<ApiResponse<PaymentEntity>> getPaymentByStripeId(
            @PathVariable @NotNull String stripePaymentIntentId) {
        
        logger.debug("{} - Getting payment by Stripe ID: {}", CONTROLLER_NAME, stripePaymentIntentId);
        
        try {
            Optional<PaymentEntity> paymentOpt = paymentService.getPaymentByStripeId(stripePaymentIntentId);
            
            if (paymentOpt.isPresent()) {
                logger.debug("{} - Payment found for Stripe ID: {}", CONTROLLER_NAME, stripePaymentIntentId);
                return ResponseEntity.ok(ApiResponse.success(paymentOpt.get(), "Payment retrieved successfully"));
            } else {
                logger.warn("{} - Payment not found for Stripe ID: {}", CONTROLLER_NAME, stripePaymentIntentId);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("{} - Error getting payment by Stripe ID: {}", CONTROLLER_NAME, stripePaymentIntentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve payment"));
        }
    }
    
    /**
     * Health check endpoint
     * 
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        logger.debug("{} - Health check requested", CONTROLLER_NAME);
        return ResponseEntity.ok(ApiResponse.success("Payment service is healthy", "Service is running"));
    }
    
    /**
     * Refund request DTO
     */
    public static class RefundRequest {
        private BigDecimal amount;
        private String reason;
        
        public RefundRequest() {}
        
        public RefundRequest(BigDecimal amount, String reason) {
            this.amount = amount;
            this.reason = reason;
        }
        
        public BigDecimal getAmount() {
            return amount;
        }
        
        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
        
        public String getReason() {
            return reason;
        }
        
        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
