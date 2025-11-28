package com.netflix.springai.cost;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class CostOptimizationEngine {

    private static final Logger logger = LoggerFactory.getLogger(CostOptimizationEngine.class);

    private final CostAnalyticsService costAnalyticsService;
    private final ModelUsageRepository modelUsageRepository;

    // Cost optimization strategies
    private enum OptimizationStrategy {
        COST_FIRST,        // Minimize cost, may sacrifice some quality
        BALANCED,          // Balance cost and quality
        QUALITY_FIRST,     // Maximize quality, cost is secondary
        PREDICTIVE         // Use ML predictions for optimization
    }

    // Model capability mappings (higher number = more capable)
    private final Map<String, Integer> modelCapabilities = Map.of(
        "gpt-4", 100,
        "gpt-3.5-turbo", 75,
        "claude-3", 95,
        "claude-2", 85,
        "text-embedding-ada-002", 70,
        "text-embedding-3-small", 60
    );

    // Model cost rates (per 1K tokens)
    private final Map<String, BigDecimal> modelCostRates = Map.of(
        "gpt-4", new BigDecimal("0.03"),
        "gpt-3.5-turbo", new BigDecimal("0.002"),
        "claude-3", new BigDecimal("0.015"),
        "claude-2", new BigDecimal("0.008"),
        "text-embedding-ada-002", new BigDecimal("0.0001"),
        "text-embedding-3-small", new BigDecimal("0.00002")
    );

    // Response cache for optimization recommendations
    private final Map<String, OptimizationRecommendation> recommendationCache = new ConcurrentHashMap<>();

    @Autowired
    public CostOptimizationEngine(
            CostAnalyticsService costAnalyticsService,
            ModelUsageRepository modelUsageRepository) {
        this.costAnalyticsService = costAnalyticsService;
        this.modelUsageRepository = modelUsageRepository;
    }

    /**
     * Select the optimal model for a given request based on cost, quality, and usage patterns
     */
    @Cacheable(value = "modelSelection", key = "#requestType + '_' + #requiredQuality + '_' + #budgetConstraint")
    public ModelSelection selectOptimalModel(String requestType, int requiredQuality, BigDecimal budgetConstraint) {
        List<String> availableModels = getModelsForRequestType(requestType);

        if (availableModels.isEmpty()) {
            throw new IllegalArgumentException("No models available for request type: " + requestType);
        }

        // Get current cost analytics
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);
        CostAnalyticsReport costReport = costAnalyticsService.generateCostReport(startDate, endDate);

        // Evaluate each model
        List<ModelEvaluation> evaluations = availableModels.stream()
            .map(model -> evaluateModel(model, requiredQuality, budgetConstraint, costReport))
            .collect(Collectors.toList());

        // Select best model based on optimization strategy
        OptimizationStrategy strategy = determineOptimizationStrategy(budgetConstraint, requiredQuality);
        ModelEvaluation bestModel = selectBestModel(evaluations, strategy);

        logger.info("Selected optimal model: {} for {} (strategy: {}, score: {})",
                   bestModel.getModelType(), requestType, strategy, bestModel.getScore());

        return new ModelSelection(
            bestModel.getModelType(),
            bestModel.getEstimatedCost(),
            bestModel.getEstimatedQuality(),
            bestModel.getScore(),
            strategy.name(),
            generateSelectionReason(bestModel, evaluations),
            LocalDateTime.now()
        );
    }

    /**
     * Generate cost optimization recommendations
     */
    public OptimizationRecommendation generateOptimizationRecommendation(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        String cacheKey = userId + "_" + startDate + "_" + endDate;

        // Check cache first
        OptimizationRecommendation cached = recommendationCache.get(cacheKey);
        if (cached != null && cached.getGeneratedAt().isAfter(LocalDateTime.now().minusMinutes(30))) {
            return cached;
        }

        CostAnalyticsReport userReport = costAnalyticsService.generateUserCostReport(userId, startDate, endDate);

        if (userReport.getTotalRequests() == 0) {
            return OptimizationRecommendation.noAction(userId, "Insufficient usage data");
        }

        List<OptimizationAction> actions = new ArrayList<>();

        // Analyze cost anomalies
        List<CostAnomaly> anomalies = costAnalyticsService.detectCostAnomalies(startDate, endDate);
        if (!anomalies.isEmpty()) {
            actions.add(new OptimizationAction(
                "INVESTIGATE_ANOMALIES",
                "Investigate " + anomalies.size() + " cost anomalies detected",
                OptimizationPriority.HIGH,
                BigDecimal.ZERO
            ));
        }

        // Analyze model usage efficiency
        Map<String, BigDecimal> costByModel = userReport.getCostByModel();
        for (Map.Entry<String, BigDecimal> entry : costByModel.entrySet()) {
            String modelType = entry.getKey();
            BigDecimal modelCost = entry.getValue();

            // Check if cheaper alternative exists
            String cheaperAlternative = findCheaperAlternative(modelType);
            if (cheaperAlternative != null) {
                BigDecimal currentRate = modelCostRates.getOrDefault(modelType, BigDecimal.ZERO);
                BigDecimal alternativeRate = modelCostRates.getOrDefault(cheaperAlternative, BigDecimal.ZERO);
                BigDecimal potentialSavings = currentRate.subtract(alternativeRate);

                if (potentialSavings.compareTo(BigDecimal.ZERO) > 0) {
                    actions.add(new OptimizationAction(
                        "SWITCH_MODEL",
                        "Consider switching from " + modelType + " to " + cheaperAlternative,
                        OptimizationPriority.MEDIUM,
                        potentialSavings
                    ));
                }
            }
        }

        // Check for caching opportunities
        if (userReport.getAverageCostPerRequest().compareTo(new BigDecimal("0.01")) > 0) {
            actions.add(new OptimizationAction(
                "IMPLEMENT_CACHING",
                "Implement response caching to reduce repeated requests",
                OptimizationPriority.MEDIUM,
                userReport.getTotalCost().multiply(new BigDecimal("0.2")) // Estimate 20% savings
            ));
        }

        // Check usage patterns for batching opportunities
        if (hasBatchingOpportunities(userId, startDate, endDate)) {
            actions.add(new OptimizationAction(
                "IMPLEMENT_BATCHING",
                "Batch similar requests to reduce per-request costs",
                OptimizationPriority.LOW,
                userReport.getTotalCost().multiply(new BigDecimal("0.1")) // Estimate 10% savings
            ));
        }

        BigDecimal totalPotentialSavings = actions.stream()
            .map(OptimizationAction::getPotentialSavings)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        OptimizationRecommendation recommendation = new OptimizationRecommendation(
            userId,
            actions,
            totalPotentialSavings,
            calculateOptimizationScore(actions),
            LocalDateTime.now()
        );

        // Cache the recommendation
        recommendationCache.put(cacheKey, recommendation);

        return recommendation;
    }

    /**
     * Apply cost optimization measures
     */
    public OptimizationResult applyOptimization(String userId, String actionId) {
        // In a real implementation, this would apply the specific optimization
        logger.info("Applying optimization action {} for user {}", actionId, userId);

        return new OptimizationResult(
            actionId,
            true,
            "Optimization applied successfully",
            LocalDateTime.now()
        );
    }

    /**
     * Get real-time cost optimization suggestions
     */
    public List<RealtimeOptimization> getRealtimeOptimizations(String modelType, long inputTokens, long outputTokens) {
        List<RealtimeOptimization> optimizations = new ArrayList<>();

        // Check if request can be served from cache
        String requestHash = generateRequestHash(modelType, inputTokens, outputTokens);
        if (isRequestCacheable(requestHash)) {
            optimizations.add(new RealtimeOptimization(
                "USE_CACHE",
                "Serve from cache instead of making API call",
                OptimizationType.CACHE,
                BigDecimal.valueOf(0.002) // Estimated cost of cache hit
            ));
        }

        // Check for model downgrade opportunities
        String cheaperModel = findCheaperAlternative(modelType);
        if (cheaperModel != null && isDowngradeSafe(modelType, cheaperModel, inputTokens, outputTokens)) {
            BigDecimal currentCost = costAnalyticsService.calculateCost(modelType, inputTokens, outputTokens);
            BigDecimal cheaperCost = costAnalyticsService.calculateCost(cheaperModel, inputTokens, outputTokens);
            BigDecimal savings = currentCost.subtract(cheaperCost);

            optimizations.add(new RealtimeOptimization(
                "DOWNGRADE_MODEL",
                "Use " + cheaperModel + " instead of " + modelType,
                OptimizationType.MODEL_SELECTION,
                savings
            ));
        }

        // Check for token optimization
        if (inputTokens > 1000) {
            optimizations.add(new RealtimeOptimization(
                "OPTIMIZE_PROMPT",
                "Consider prompt compression to reduce token usage",
                OptimizationType.PROMPT_OPTIMIZATION,
                BigDecimal.valueOf(0.001) // Estimated savings
            ));
        }

        return optimizations.stream()
            .sorted((a, b) -> b.getPotentialSavings().compareTo(a.getPotentialSavings()))
            .collect(Collectors.toList());
    }

    private List<String> getModelsForRequestType(String requestType) {
        // Return appropriate models based on request type
        return switch (requestType.toUpperCase()) {
            case "CHAT", "COMPLETION" -> List.of("gpt-4", "gpt-3.5-turbo", "claude-3", "claude-2");
            case "EMBEDDING" -> List.of("text-embedding-ada-002", "text-embedding-3-small");
            case "MODERATION" -> List.of("text-moderation-stable", "text-moderation-latest");
            default -> List.of("gpt-3.5-turbo"); // Default fallback
        };
    }

    private ModelEvaluation evaluateModel(String modelType, int requiredQuality, BigDecimal budgetConstraint,
                                        CostAnalyticsReport costReport) {
        BigDecimal costRate = modelCostRates.getOrDefault(modelType, BigDecimal.ZERO);
        Integer capability = modelCapabilities.getOrDefault(modelType, 50);

        // Calculate quality score (0-100)
        double qualityScore = Math.min(100.0, capability.doubleValue());

        // Calculate cost efficiency (lower cost = higher score)
        double costEfficiency = Math.max(0.0, 100.0 - (costRate.doubleValue() * 10000));

        // Check budget constraint
        boolean withinBudget = budgetConstraint.compareTo(BigDecimal.ZERO) == 0 ||
                              costRate.compareTo(budgetConstraint) <= 0;

        // Calculate overall score based on requirements
        double score;
        if (budgetConstraint.compareTo(BigDecimal.ZERO) > 0) {
            // Budget-constrained: prioritize cost efficiency
            score = (costEfficiency * 0.7) + (qualityScore * 0.3);
        } else {
            // Quality-focused: prioritize capability
            score = (qualityScore * 0.7) + (costEfficiency * 0.3);
        }

        // Penalize if below required quality
        if (qualityScore < requiredQuality) {
            score *= 0.5;
        }

        // Penalize if over budget
        if (!withinBudget) {
            score *= 0.3;
        }

        return new ModelEvaluation(
            modelType,
            costRate,
            qualityScore,
            score,
            withinBudget
        );
    }

    private OptimizationStrategy determineOptimizationStrategy(BigDecimal budgetConstraint, int requiredQuality) {
        if (budgetConstraint.compareTo(BigDecimal.ZERO) > 0) {
            return OptimizationStrategy.COST_FIRST;
        } else if (requiredQuality >= 90) {
            return OptimizationStrategy.QUALITY_FIRST;
        } else {
            return OptimizationStrategy.BALANCED;
        }
    }

    private ModelEvaluation selectBestModel(List<ModelEvaluation> evaluations, OptimizationStrategy strategy) {
        return evaluations.stream()
            .filter(ModelEvaluation::isWithinBudget)
            .max((a, b) -> {
                switch (strategy) {
                    case OptimizationStrategy.COST_FIRST:
                        // Prefer lowest cost
                        return Double.compare(
                            modelCostRates.getOrDefault(b.getModelType(), BigDecimal.ZERO).doubleValue(),
                            modelCostRates.getOrDefault(a.getModelType(), BigDecimal.ZERO).doubleValue()
                        );
                    case OptimizationStrategy.QUALITY_FIRST:
                        // Prefer highest quality
                        return Double.compare(a.getQualityScore(), b.getQualityScore());
                    case OptimizationStrategy.BALANCED:
                    default:
                        // Use overall score
                        return Double.compare(a.getScore(), b.getScore());
                }
            })
            .orElse(evaluations.get(0)); // Fallback to first if no budget-compliant options
    }

    private String generateSelectionReason(ModelEvaluation selected, List<ModelEvaluation> allEvaluations) {
        StringBuilder reason = new StringBuilder();
        reason.append("Selected ").append(selected.getModelType())
              .append(" with score ").append(String.format("%.1f", selected.getScore()))
              .append(". ");

        if (allEvaluations.size() > 1) {
            reason.append("Evaluated ").append(allEvaluations.size()).append(" models. ");
        }

        BigDecimal cost = selected.getEstimatedCost();
        if (cost.compareTo(BigDecimal.ZERO) > 0) {
            reason.append("Estimated cost: $").append(cost.setScale(4, RoundingMode.HALF_UP));
        }

        return reason.toString();
    }

    private String findCheaperAlternative(String currentModel) {
        BigDecimal currentRate = modelCostRates.getOrDefault(currentModel, BigDecimal.ZERO);

        return modelCostRates.entrySet().stream()
            .filter(entry -> !entry.getKey().equals(currentModel))
            .filter(entry -> entry.getValue().compareTo(currentRate) < 0)
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    private boolean hasBatchingOpportunities(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        // Check if user has multiple similar requests in short time windows
        List<ModelUsage> usages = modelUsageRepository
            .findByUserIdAndTimestampBetween(userId, startDate, endDate);

        // Simple heuristic: if more than 5 requests in any 5-minute window, suggest batching
        Map<LocalDateTime, Long> requestsPer5Minutes = usages.stream()
            .collect(Collectors.groupingBy(
                usage -> usage.getTimestamp().truncatedTo(java.time.temporal.ChronoUnit.MINUTES)
                    .withMinute(usage.getTimestamp().getMinute() / 5 * 5),
                Collectors.counting()
            ));

        return requestsPer5Minutes.values().stream().anyMatch(count -> count > 5);
    }

    private String generateRequestHash(String modelType, long inputTokens, long outputTokens) {
        return modelType + "_" + inputTokens + "_" + outputTokens;
    }

    private boolean isRequestCacheable(String requestHash) {
        // In a real implementation, check against a cache
        return false; // Placeholder
    }

    private boolean isDowngradeSafe(String currentModel, String cheaperModel, long inputTokens, long outputTokens) {
        // Check if downgrading is safe based on model capabilities and request size
        Integer currentCapability = modelCapabilities.getOrDefault(currentModel, 50);
        Integer cheaperCapability = modelCapabilities.getOrDefault(cheaperModel, 50);

        // Only downgrade if capability difference is not too large and request is not too complex
        return (currentCapability - cheaperCapability) <= 20 && (inputTokens + outputTokens) < 2000;
    }

    private double calculateOptimizationScore(List<OptimizationAction> actions) {
        if (actions.isEmpty()) return 0.0;

        return actions.stream()
            .mapToDouble(action -> {
                double priorityMultiplier = switch (action.getPriority()) {
                    case HIGH -> 1.0;
                    case MEDIUM -> 0.7;
                    case LOW -> 0.4;
                };
                return action.getPotentialSavings().doubleValue() * priorityMultiplier;
            })
            .sum();
    }

    // Nested classes for data structures
    private static class ModelEvaluation {
        private final String modelType;
        private final BigDecimal estimatedCost;
        private final double qualityScore;
        private final double score;
        private final boolean withinBudget;

        public ModelEvaluation(String modelType, BigDecimal estimatedCost, double qualityScore,
                             double score, boolean withinBudget) {
            this.modelType = modelType;
            this.estimatedCost = estimatedCost;
            this.qualityScore = qualityScore;
            this.score = score;
            this.withinBudget = withinBudget;
        }

        // Getters
        public String getModelType() { return modelType; }
        public BigDecimal getEstimatedCost() { return estimatedCost; }
        public double getQualityScore() { return qualityScore; }
        public double getScore() { return score; }
        public boolean isWithinBudget() { return withinBudget; }
    }
}
