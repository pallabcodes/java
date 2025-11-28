package com.example.kotlinpay.risk.api

import com.example.kotlinpay.risk.domain.RiskDecision
import com.example.kotlinpay.risk.domain.RiskEvaluationRequest
import com.example.kotlinpay.risk.ml.FraudDetectionML
import com.example.kotlinpay.risk.ml.ModelTrainingService
import com.example.kotlinpay.risk.ml.RealTimeRuleEngine
import com.example.kotlinpay.risk.ml.TransactionData
import com.example.kotlinpay.risk.service.RiskEvaluationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/risk")
@Tag(name = "Risk Evaluation", description = "Payment risk evaluation API")
class RiskController(
    private val riskEvaluationService: RiskEvaluationService,
    private val fraudDetectionML: FraudDetectionML,
    private val modelTrainingService: ModelTrainingService,
    private val realTimeRuleEngine: RealTimeRuleEngine
) {

    private val logger = LoggerFactory.getLogger(RiskController::class.java)

    @PostMapping("/decisions")
    @PreAuthorize("hasRole('USER') or hasRole('SERVICE') or hasRole('ADMIN')")
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

    @PostMapping("/ml-evaluation")
    @PreAuthorize("hasRole('USER') or hasRole('SERVICE') or hasRole('ADMIN')")
    @Operation(summary = "ML-based fraud evaluation", description = "Evaluates payment risk using machine learning models")
    fun evaluateFraudWithML(@Valid @RequestBody request: MLFraudEvaluationRequest): ResponseEntity<MLFraudEvaluationResponse> {
        logger.info("Received ML fraud evaluation request for transaction: {}", request.transactionId)

        val transaction = TransactionData(
            id = request.transactionId,
            userId = request.userId,
            amount = request.amount,
            merchantId = request.merchantId,
            merchantCategory = request.merchantCategory,
            paymentMethod = request.paymentMethod,
            country = request.country,
            city = request.city,
            timestamp = request.timestamp ?: LocalDateTime.now()
        )

        val evaluation = fraudDetectionML.evaluateFraudRisk(transaction)

        val response = MLFraudEvaluationResponse(
            transactionId = evaluation.transactionId,
            fraudScore = evaluation.fraudScore,
            riskLevel = evaluation.riskLevel.name,
            confidence = evaluation.confidence,
            evaluationTimeMs = evaluation.evaluationTimeMs,
            featuresUsed = evaluation.featuresUsed,
            modelsUsed = evaluation.modelsUsed,
            rulesTriggered = evaluation.rulesTriggered,
            explanation = evaluation.explanation,
            evaluatedAt = evaluation.timestamp
        )

        logger.info("ML fraud evaluation completed for transaction {}: score={}, risk={}, time={}ms",
            evaluation.transactionId, evaluation.fraudScore, evaluation.riskLevel,
            evaluation.evaluationTimeMs)

        return ResponseEntity.ok(response)
    }

    @PostMapping("/train-model")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Train ML model", description = "Triggers training of a machine learning model")
    fun trainModel(@Valid @RequestBody request: ModelTrainingRequest): ResponseEntity<ModelTrainingResponse> {
        logger.info("Received model training request for algorithm: {}", request.algorithm)

        try {
            val algorithm = com.example.kotlinpay.risk.ml.Algorithm.valueOf(request.algorithm.uppercase())

            // Trigger async training
            val trainingFuture = modelTrainingService.trainModel(
                algorithm = algorithm,
                trainingData = emptyList(), // In production, load from database
                hyperparameters = request.hyperparameters
            )

            // For demo purposes, we'll wait for completion. In production, return immediately with job ID
            val trainingResult = trainingFuture.get()

            val response = when (trainingResult) {
                is com.example.kotlinpay.risk.ml.ModelTrainingResult.Success -> {
                    ModelTrainingResponse(
                        success = true,
                        modelName = trainingResult.model.name,
                        accuracy = trainingResult.metrics.accuracy,
                        trainingTimeMs = trainingResult.trainingTimeMs,
                        message = "Model trained successfully"
                    )
                }
                is com.example.kotlinpay.risk.ml.ModelTrainingResult.Failure -> {
                    ModelTrainingResponse(
                        success = false,
                        message = trainingResult.error
                    )
                }
            }

            return ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("Model training failed", e)
            return ResponseEntity.ok(ModelTrainingResponse(
                success = false,
                message = "Training failed: ${e.message}"
            ))
        }
    }

    @GetMapping("/ml-stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SERVICE')")
    @Operation(summary = "Get ML statistics", description = "Returns machine learning fraud detection statistics")
    fun getMLStats(): ResponseEntity<MLStatisticsResponse> {
        val fraudStats = fraudDetectionML.getFraudDetectionStats()
        val trainingStats = modelTrainingService.getTrainingStats()
        val ruleStats = realTimeRuleEngine.getRuleEngineStats()

        val response = MLStatisticsResponse(
            fraudDetection = FraudDetectionStatsDto(
                totalModels = fraudStats.totalModels,
                activeModels = fraudStats.activeModels,
                evaluationsLast24Hours = fraudStats.evaluationsLast24Hours,
                averageEvaluationTime = fraudStats.averageEvaluationTime,
                modelAccuracy = fraudStats.modelAccuracy,
                falsePositiveRate = fraudStats.falsePositiveRate,
                falseNegativeRate = fraudStats.falseNegativeRate
            ),
            training = TrainingStatsDto(
                totalModels = trainingStats.totalModels,
                activeExperiments = trainingStats.activeExperiments,
                recentTrainings = trainingStats.recentTrainings,
                successfulDeployments = trainingStats.successfulDeployments,
                averageTrainingTime = trainingStats.averageTrainingTime,
                modelAccuracy = trainingStats.modelAccuracy
            ),
            rules = RuleStatsDto(
                totalRules = ruleStats.totalRules,
                evaluationsLast24Hours = ruleStats.evaluationsLast24Hours,
                mostTriggeredRule = ruleStats.mostTriggeredRule,
                averageRuleScore = ruleStats.averageRuleScore,
                ruleErrorRate = ruleStats.ruleErrorRate
            )
        )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns service health status")
    fun health(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(mapOf(
            "status" to "UP",
            "service" to "risk-service",
            "ml_enabled" to true,
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

// ML Fraud Evaluation DTOs
data class MLFraudEvaluationRequest(
    val transactionId: String,
    val userId: String,
    val amount: Double,
    val merchantId: String,
    val merchantCategory: Int,
    val paymentMethod: String,
    val country: String,
    val city: String,
    val timestamp: LocalDateTime? = null
)

data class MLFraudEvaluationResponse(
    val transactionId: String,
    val fraudScore: Double,
    val riskLevel: String,
    val confidence: Double,
    val evaluationTimeMs: Double,
    val featuresUsed: Int,
    val modelsUsed: Int,
    val rulesTriggered: Int,
    val explanation: String,
    val evaluatedAt: LocalDateTime
)

// Model Training DTOs
data class ModelTrainingRequest(
    val algorithm: String,
    val hyperparameters: Map<String, Any> = emptyMap()
)

data class ModelTrainingResponse(
    val success: Boolean,
    val modelName: String? = null,
    val accuracy: Double? = null,
    val trainingTimeMs: Long? = null,
    val message: String
)

// Statistics DTOs
data class MLStatisticsResponse(
    val fraudDetection: FraudDetectionStatsDto,
    val training: TrainingStatsDto,
    val rules: RuleStatsDto
)

data class FraudDetectionStatsDto(
    val totalModels: Int,
    val activeModels: Int,
    val evaluationsLast24Hours: Int,
    val averageEvaluationTime: Double,
    val modelAccuracy: Double,
    val falsePositiveRate: Double,
    val falseNegativeRate: Double
)

data class TrainingStatsDto(
    val totalModels: Int,
    val activeExperiments: Int,
    val recentTrainings: Int,
    val successfulDeployments: Int,
    val averageTrainingTime: Double,
    val modelAccuracy: Double
)

data class RuleStatsDto(
    val totalRules: Int,
    val evaluationsLast24Hours: Int,
    val mostTriggeredRule: String?,
    val averageRuleScore: Double,
    val ruleErrorRate: Double
)
