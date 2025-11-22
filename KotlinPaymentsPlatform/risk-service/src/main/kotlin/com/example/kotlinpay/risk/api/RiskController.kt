package com.example.kotlinpay.risk.api

import com.example.kotlinpay.risk.domain.RiskDecision
import com.example.kotlinpay.risk.domain.RiskEvaluationRequest
import com.example.kotlinpay.risk.service.RiskEvaluationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/risk")
@Tag(name = "Risk Evaluation", description = "Payment risk evaluation API")
class RiskController(
    private val riskEvaluationService: RiskEvaluationService
) {

    private val logger = LoggerFactory.getLogger(RiskController::class.java)

    @PostMapping("/decisions")
    @Operation(summary = "Evaluate payment risk", description = "Evaluates the risk level of a payment transaction")
    fun evaluateRisk(@Valid @RequestBody request: RiskEvaluationRequest): ResponseEntity<RiskDecisionResponse> {
        logger.info("Received risk evaluation request for payment: {}", request.paymentId)

        val result = riskEvaluationService.evaluateRisk(request)

        val response = RiskDecisionResponse(
            paymentId = result.paymentId,
            decision = result.decision,
            riskScore = result.riskScore,
            riskLevel = result.riskLevel,
            reasons = result.reasons,
            evaluatedAt = result.evaluatedAt
        )

        logger.info("Risk evaluation completed for payment {}: decision={}, score={}",
            result.paymentId, result.decision, result.riskScore)

        return ResponseEntity.ok(response)
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns service health status")
    fun health(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(mapOf(
            "status" to "UP",
            "service" to "risk-service",
            "timestamp" to java.time.LocalDateTime.now()
        ))
    }
}

// Request DTO
data class RiskEvaluationRequestDto(
    val paymentId: String,
    val amount: String, // Using String to avoid BigDecimal serialization issues
    val currency: String,
    val customerId: String,
    val merchantId: String,
    val cardLastFour: String,
    val countryCode: String,
    val ipAddress: String,
    val userAgent: String
)

// Response DTO
data class RiskDecisionResponse(
    val paymentId: String,
    val decision: RiskDecision,
    val riskScore: Int,
    val riskLevel: String,
    val reasons: List<String>,
    val evaluatedAt: java.time.LocalDateTime
)
