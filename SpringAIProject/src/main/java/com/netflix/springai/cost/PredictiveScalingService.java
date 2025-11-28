package com.netflix.springai.cost;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class PredictiveScalingService {

    private static final Logger logger = LoggerFactory.getLogger(PredictiveScalingService.class);

    private final ModelUsageRepository modelUsageRepository;
    private final ScalingRecommendationRepository scalingRecommendationRepository;

    // Scaling parameters
    private static final int HISTORICAL_DAYS = 30;
    private static final int PREDICTION_DAYS = 7;
    private static final double SCALE_UP_THRESHOLD = 0.8; // 80% utilization
    private static final double SCALE_DOWN_THRESHOLD = 0.3; // 30% utilization
    private static final int MIN_INSTANCES = 1;
    private static final int MAX_INSTANCES = 10;

    @Autowired
    public PredictiveScalingService(
            ModelUsageRepository modelUsageRepository,
            ScalingRecommendationRepository scalingRecommendationRepository) {
        this.modelUsageRepository = modelUsageRepository;
        this.scalingRecommendationRepository = scalingRecommendationRepository;
    }

    @Scheduled(fixedRate = 3600000) // Run every hour
    public void analyzeAndRecommendScaling() {
        logger.info("Starting predictive scaling analysis");

        try {
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(HISTORICAL_DAYS);

            // Analyze usage patterns for each model type
            Set<String> modelTypes = modelUsageRepository.findDistinctModelTypes();
            for (String modelType : modelTypes) {
                ScalingRecommendation recommendation = analyzeModelScaling(modelType, startDate, endDate);
                if (recommendation != null) {
                    scalingRecommendationRepository.save(recommendation);
                    logger.info("Generated scaling recommendation for {}: {} instances (confidence: {}%)",
                               modelType, recommendation.getRecommendedInstances(), recommendation.getConfidence());
                }
            }

        } catch (Exception e) {
            logger.error("Error during scaling analysis", e);
        }
    }

    public ScalingRecommendation analyzeModelScaling(String modelType, LocalDateTime startDate, LocalDateTime endDate) {
        // Get historical usage data
        List<ModelUsage> historicalUsage = modelUsageRepository
            .findByModelTypeAndTimestampBetween(modelType, startDate, endDate);

        if (historicalUsage.size() < 10) { // Need minimum data points
            logger.debug("Insufficient data for scaling analysis of model: {}", modelType);
            return null;
        }

        // Analyze usage patterns
        UsagePatternAnalysis analysis = analyzeUsagePatterns(historicalUsage);

        // Generate predictions
        ScalingPrediction prediction = predictFutureUsage(analysis);

        // Determine scaling recommendation
        int currentInstances = getCurrentInstanceCount(modelType);
        int recommendedInstances = calculateRecommendedInstances(prediction, currentInstances);

        if (recommendedInstances == currentInstances) {
            return null; // No scaling needed
        }

        ScalingRecommendation recommendation = new ScalingRecommendation();
        recommendation.setModelType(modelType);
        recommendation.setCurrentInstances(currentInstances);
        recommendation.setRecommendedInstances(recommendedInstances);
        recommendation.setScalingDirection(recommendedInstances > currentInstances ? "UP" : "DOWN");
        recommendation.setPredictedLoad(prediction.getPredictedLoad());
        recommendation.setConfidence(prediction.getConfidence());
        recommendation.setReason(generateScalingReason(prediction, analysis));
        recommendation.setGeneratedAt(LocalDateTime.now());
        recommendation.setValidUntil(LocalDateTime.now().plusHours(1)); // Valid for 1 hour

        return recommendation;
    }

    private UsagePatternAnalysis analyzeUsagePatterns(List<ModelUsage> usages) {
        // Group by hour of day and day of week for pattern analysis
        Map<Integer, List<ModelUsage>> hourlyUsage = usages.stream()
            .collect(Collectors.groupingBy(usage -> usage.getTimestamp().getHour()));

        Map<Integer, List<ModelUsage>> dailyUsage = usages.stream()
            .collect(Collectors.groupingBy(usage -> usage.getTimestamp().getDayOfWeek().getValue()));

        // Calculate average usage per hour
        Map<Integer, Double> avgHourlyUsage = hourlyUsage.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream()
                    .mapToDouble(usage -> usage.getTotalTokens().doubleValue())
                    .average().orElse(0.0)
            ));

        // Calculate peak hours
        List<Integer> peakHours = avgHourlyUsage.entrySet().stream()
            .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
            .limit(3)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        // Calculate usage trends
        List<ModelUsage> sortedUsages = usages.stream()
            .sorted(Comparator.comparing(ModelUsage::getTimestamp))
            .collect(Collectors.toList());

        double trend = calculateUsageTrend(sortedUsages);

        // Calculate volatility (coefficient of variation)
        double mean = sortedUsages.stream()
            .mapToDouble(usage -> usage.getTotalTokens().doubleValue())
            .average().orElse(0.0);

        double variance = sortedUsages.stream()
            .mapToDouble(usage -> Math.pow(usage.getTotalTokens().doubleValue() - mean, 2))
            .average().orElse(0.0);

        double volatility = mean > 0 ? Math.sqrt(variance) / mean : 0.0;

        return new UsagePatternAnalysis(
            avgHourlyUsage,
            peakHours,
            trend,
            volatility,
            mean,
            sortedUsages.size()
        );
    }

    private ScalingPrediction predictFutureUsage(UsagePatternAnalysis analysis) {
        // Simple time series forecasting using moving average with trend
        double currentAverage = analysis.getMeanUsage();
        double trend = analysis.getTrend();

        // Predict load for next hour based on current time
        int currentHour = LocalDateTime.now().getHour();
        double hourlyMultiplier = analysis.getAvgHourlyUsage().getOrDefault(currentHour, 1.0) / analysis.getMeanUsage();

        // Apply trend adjustment (assume linear trend continues)
        double predictedLoad = currentAverage * hourlyMultiplier * (1 + trend);

        // Calculate confidence based on data points and volatility
        double confidence = Math.max(0.0, Math.min(1.0,
            (analysis.getDataPoints() / 100.0) * (1 - analysis.getVolatility())));

        return new ScalingPrediction(predictedLoad, confidence * 100);
    }

    private int calculateRecommendedInstances(ScalingPrediction prediction, int currentInstances) {
        double predictedLoad = prediction.getPredictedLoad();

        // Estimate required instances based on load
        // Assume each instance can handle 1000 tokens per minute
        double tokensPerMinutePerInstance = 1000.0;
        int requiredInstances = (int) Math.ceil(predictedLoad / tokensPerMinutePerInstance);

        // Apply thresholds for scaling decisions
        double utilizationRate = predictedLoad / (currentInstances * tokensPerMinutePerInstance);

        int recommendedInstances = currentInstances;

        if (utilizationRate > SCALE_UP_THRESHOLD) {
            // Scale up
            recommendedInstances = Math.min(MAX_INSTANCES,
                Math.max(currentInstances + 1, (int) Math.ceil(predictedLoad / tokensPerMinutePerInstance)));
        } else if (utilizationRate < SCALE_DOWN_THRESHOLD && currentInstances > MIN_INSTANCES) {
            // Scale down (but be conservative)
            recommendedInstances = Math.max(MIN_INSTANCES, currentInstances - 1);
        }

        return Math.max(MIN_INSTANCES, Math.min(MAX_INSTANCES, recommendedInstances));
    }

    private String generateScalingReason(ScalingPrediction prediction, UsagePatternAnalysis analysis) {
        StringBuilder reason = new StringBuilder();

        reason.append("Predicted load: ").append(String.format("%.0f", prediction.getPredictedLoad())).append(" tokens/minute. ");

        if (analysis.getTrend() > 0.1) {
            reason.append("Upward usage trend detected. ");
        } else if (analysis.getTrend() < -0.1) {
            reason.append("Downward usage trend detected. ");
        }

        if (!analysis.getPeakHours().isEmpty()) {
            reason.append("Peak hours: ").append(analysis.getPeakHours()).append(". ");
        }

        reason.append("Confidence: ").append(String.format("%.1f", prediction.getConfidence())).append("%.");

        return reason.toString();
    }

    private double calculateUsageTrend(List<ModelUsage> usages) {
        if (usages.size() < 2) return 0.0;

        // Simple linear regression to calculate trend
        int n = usages.size();
        List<Double> x = IntStream.range(0, n).mapToDouble(i -> i.doubleValue()).boxed().collect(Collectors.toList());
        List<Double> y = usages.stream()
            .mapToDouble(usage -> usage.getTotalTokens().doubleValue())
            .boxed()
            .collect(Collectors.toList());

        double sumX = x.stream().mapToDouble(Double::doubleValue).sum();
        double sumY = y.stream().mapToDouble(Double::doubleValue).sum();
        double sumXY = IntStream.range(0, n)
            .mapToDouble(i -> x.get(i) * y.get(i))
            .sum();
        double sumXX = x.stream().mapToDouble(xi -> xi * xi).sum();

        double slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);

        // Normalize trend as percentage change
        double meanY = sumY / n;
        return meanY > 0 ? slope / meanY : 0.0;
    }

    private int getCurrentInstanceCount(String modelType) {
        // In a real implementation, this would query your deployment system
        // For now, return a default value
        return 2; // Assume 2 instances by default
    }

    public List<ScalingRecommendation> getActiveRecommendations() {
        return scalingRecommendationRepository
            .findByValidUntilAfterAndAppliedFalseOrderByGeneratedAtDesc(LocalDateTime.now());
    }

    public void applyScalingRecommendation(Long recommendationId) {
        ScalingRecommendation recommendation = scalingRecommendationRepository.findById(recommendationId)
            .orElseThrow(() -> new IllegalArgumentException("Recommendation not found"));

        if (recommendation.isApplied()) {
            throw new IllegalStateException("Recommendation already applied");
        }

        // In a real implementation, this would trigger actual scaling via deployment API
        logger.info("Applying scaling recommendation: {} instances for {}",
                   recommendation.getRecommendedInstances(), recommendation.getModelType());

        recommendation.setApplied(true);
        recommendation.setAppliedAt(LocalDateTime.now());
        scalingRecommendationRepository.save(recommendation);
    }

    public ScalingDashboardData getScalingDashboardData() {
        List<ScalingRecommendation> activeRecommendations = getActiveRecommendations();

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);

        // Get recent scaling actions
        List<ScalingRecommendation> recentActions = scalingRecommendationRepository
            .findByAppliedAtBetween(startDate, endDate);

        // Calculate scaling efficiency metrics
        Map<String, ScalingMetrics> modelMetrics = calculateScalingMetrics(recentActions);

        return new ScalingDashboardData(
            activeRecommendations.size(),
            recentActions.size(),
            modelMetrics,
            LocalDateTime.now()
        );
    }

    private Map<String, ScalingMetrics> calculateScalingMetrics(List<ScalingRecommendation> recommendations) {
        return recommendations.stream()
            .collect(Collectors.groupingBy(ScalingRecommendation::getModelType))
            .entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    List<ScalingRecommendation> modelRecommendations = entry.getValue();

                    long successfulScalings = modelRecommendations.stream()
                        .filter(r -> r.getConfidence() > 70.0)
                        .count();

                    double avgConfidence = modelRecommendations.stream()
                        .mapToDouble(ScalingRecommendation::getConfidence)
                        .average().orElse(0.0);

                    long scaleUpActions = modelRecommendations.stream()
                        .filter(r -> "UP".equals(r.getScalingDirection()))
                        .count();

                    long scaleDownActions = modelRecommendations.stream()
                        .filter(r -> "DOWN".equals(r.getScalingDirection()))
                        .count();

                    return new ScalingMetrics(
                        modelRecommendations.size(),
                        successfulScalings,
                        avgConfidence,
                        scaleUpActions,
                        scaleDownActions
                    );
                }
            ));
    }

    // Nested classes for data structures
    private static class UsagePatternAnalysis {
        private final Map<Integer, Double> avgHourlyUsage;
        private final List<Integer> peakHours;
        private final double trend;
        private final double volatility;
        private final double meanUsage;
        private final int dataPoints;

        public UsagePatternAnalysis(Map<Integer, Double> avgHourlyUsage, List<Integer> peakHours,
                                  double trend, double volatility, double meanUsage, int dataPoints) {
            this.avgHourlyUsage = avgHourlyUsage;
            this.peakHours = peakHours;
            this.trend = trend;
            this.volatility = volatility;
            this.meanUsage = meanUsage;
            this.dataPoints = dataPoints;
        }

        // Getters
        public Map<Integer, Double> getAvgHourlyUsage() { return avgHourlyUsage; }
        public List<Integer> getPeakHours() { return peakHours; }
        public double getTrend() { return trend; }
        public double getVolatility() { return volatility; }
        public double getMeanUsage() { return meanUsage; }
        public int getDataPoints() { return dataPoints; }
    }

    private static class ScalingPrediction {
        private final double predictedLoad;
        private final double confidence;

        public ScalingPrediction(double predictedLoad, double confidence) {
            this.predictedLoad = predictedLoad;
            this.confidence = confidence;
        }

        public double getPredictedLoad() { return predictedLoad; }
        public double getConfidence() { return confidence; }
    }
}
