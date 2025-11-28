package com.netflix.springai.cost;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CostAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(CostAnalyticsService.class);

    private final AICostRepository aiCostRepository;
    private final ModelUsageRepository modelUsageRepository;
    private final CostAlertRepository costAlertRepository;

    // Cost rates per model (per 1K tokens) - configurable
    private final Map<String, BigDecimal> modelCostRates = Map.of(
        "gpt-4", new BigDecimal("0.03"),
        "gpt-3.5-turbo", new BigDecimal("0.002"),
        "text-embedding-ada-002", new BigDecimal("0.0001"),
        "claude-3", new BigDecimal("0.015"),
        "claude-2", new BigDecimal("0.008")
    );

    @Autowired
    public CostAnalyticsService(
            AICostRepository aiCostRepository,
            ModelUsageRepository modelUsageRepository,
            CostAlertRepository costAlertRepository) {
        this.aiCostRepository = aiCostRepository;
        this.modelUsageRepository = modelUsageRepository;
        this.costAlertRepository = costAlertRepository;
    }

    @Transactional
    public void recordUsage(String modelType, String modelVersion, String operation,
                           long inputTokens, long outputTokens, BigDecimal cost, String userId) {

        ModelUsage usage = new ModelUsage();
        usage.setModelType(modelType);
        usage.setModelVersion(modelVersion);
        usage.setOperation(operation);
        usage.setInputTokens(inputTokens);
        usage.setOutputTokens(outputTokens);
        usage.setTotalTokens(inputTokens + outputTokens);
        usage.setCost(cost);
        usage.setUserId(userId);
        usage.setTimestamp(LocalDateTime.now());

        modelUsageRepository.save(usage);

        // Check for cost alerts
        checkCostAlerts(usage);

        logger.debug("Recorded AI usage: {} {} tokens, cost: ${}",
                    modelType, usage.getTotalTokens(), cost);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "costAnalytics", key = "#startDate.toString() + '_' + #endDate.toString()")
    public CostAnalyticsReport generateCostReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<ModelUsage> usages = modelUsageRepository.findByTimestampBetween(startDate, endDate);

        return calculateCostAnalytics(usages, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public CostAnalyticsReport generateUserCostReport(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<ModelUsage> usages = modelUsageRepository.findByUserIdAndTimestampBetween(userId, startDate, endDate);

        return calculateCostAnalytics(usages, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public CostAnalyticsReport generateModelCostReport(String modelType, LocalDateTime startDate, LocalDateTime endDate) {
        List<ModelUsage> usages = modelUsageRepository.findByModelTypeAndTimestampBetween(modelType, startDate, endDate);

        return calculateCostAnalytics(usages, startDate, endDate);
    }

    private CostAnalyticsReport calculateCostAnalytics(List<ModelUsage> usages, LocalDateTime startDate, LocalDateTime endDate) {
        if (usages.isEmpty()) {
            return CostAnalyticsReport.empty(startDate, endDate);
        }

        // Total costs and usage
        BigDecimal totalCost = usages.stream()
            .map(ModelUsage::getCost)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalTokens = usages.stream()
            .mapToLong(ModelUsage::getTotalTokens)
            .sum();

        long totalRequests = usages.size();

        // Cost breakdown by model type
        Map<String, BigDecimal> costByModel = usages.stream()
            .collect(Collectors.groupingBy(
                ModelUsage::getModelType,
                Collectors.reducing(BigDecimal.ZERO, ModelUsage::getCost, BigDecimal::add)
            ));

        // Cost breakdown by operation
        Map<String, BigDecimal> costByOperation = usages.stream()
            .collect(Collectors.groupingBy(
                ModelUsage::getOperation,
                Collectors.reducing(BigDecimal.ZERO, ModelUsage::getCost, BigDecimal::add)
            ));

        // Usage trends (daily)
        Map<LocalDateTime, BigDecimal> dailyCosts = usages.stream()
            .collect(Collectors.groupingBy(
                usage -> usage.getTimestamp().truncatedTo(ChronoUnit.DAYS),
                Collectors.reducing(BigDecimal.ZERO, ModelUsage::getCost, BigDecimal::add)
            ));

        // Top users by cost
        Map<String, BigDecimal> topUsersByCost = usages.stream()
            .collect(Collectors.groupingBy(
                ModelUsage::getUserId,
                Collectors.reducing(BigDecimal.ZERO, ModelUsage::getCost, BigDecimal::add)
            ))
            .entrySet().stream()
            .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
            .limit(10)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));

        // Cost efficiency metrics
        BigDecimal averageCostPerToken = totalTokens > 0 ?
            totalCost.divide(BigDecimal.valueOf(totalTokens), 6, RoundingMode.HALF_UP) :
            BigDecimal.ZERO;

        BigDecimal averageCostPerRequest = totalRequests > 0 ?
            totalCost.divide(BigDecimal.valueOf(totalRequests), 4, RoundingMode.HALF_UP) :
            BigDecimal.ZERO;

        // Projected costs (simple linear extrapolation)
        CostProjection projection = calculateCostProjection(usages, startDate, endDate);

        return new CostAnalyticsReport(
            startDate,
            endDate,
            totalCost,
            totalTokens,
            totalRequests,
            costByModel,
            costByOperation,
            dailyCosts,
            topUsersByCost,
            averageCostPerToken,
            averageCostPerRequest,
            projection,
            LocalDateTime.now()
        );
    }

    private CostProjection calculateCostProjection(List<ModelUsage> usages, LocalDateTime startDate, LocalDateTime endDate) {
        long daysInPeriod = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (daysInPeriod < 2) {
            return new CostProjection(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        // Calculate daily average
        BigDecimal totalCost = usages.stream()
            .map(ModelUsage::getCost)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal dailyAverage = totalCost.divide(BigDecimal.valueOf(daysInPeriod), 4, RoundingMode.HALF_UP);

        // Project for next 30 days
        BigDecimal monthlyProjection = dailyAverage.multiply(BigDecimal.valueOf(30));

        // Calculate trend (compare first half vs second half)
        long midPoint = startDate.plusDays(daysInPeriod / 2).toLocalDate().toEpochDay();

        BigDecimal firstHalfCost = usages.stream()
            .filter(u -> u.getTimestamp().toLocalDate().toEpochDay() < midPoint)
            .map(ModelUsage::getCost)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal secondHalfCost = usages.stream()
            .filter(u -> u.getTimestamp().toLocalDate().toEpochDay() >= midPoint)
            .map(ModelUsage::getCost)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal growthRate = BigDecimal.ZERO;
        if (firstHalfCost.compareTo(BigDecimal.ZERO) > 0) {
            growthRate = secondHalfCost.subtract(firstHalfCost)
                .divide(firstHalfCost, 4, RoundingMode.HALF_UP);
        }

        return new CostProjection(monthlyProjection, growthRate, dailyAverage);
    }

    @Transactional(readOnly = true)
    public List<CostAnomaly> detectCostAnomalies(LocalDateTime startDate, LocalDateTime endDate) {
        List<ModelUsage> usages = modelUsageRepository.findByTimestampBetween(startDate, endDate);

        if (usages.size() < 7) { // Need at least a week of data
            return Collections.emptyList();
        }

        // Group by day
        Map<LocalDateTime, BigDecimal> dailyCosts = usages.stream()
            .collect(Collectors.groupingBy(
                usage -> usage.getTimestamp().truncatedTo(ChronoUnit.DAYS),
                Collectors.reducing(BigDecimal.ZERO, ModelUsage::getCost, BigDecimal::add)
            ));

        // Calculate rolling average and standard deviation
        List<BigDecimal> dailyCostValues = new ArrayList<>(dailyCosts.values());
        BigDecimal mean = dailyCostValues.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(dailyCostValues.size()), 4, RoundingMode.HALF_UP);

        BigDecimal variance = dailyCostValues.stream()
            .map(cost -> cost.subtract(mean).pow(2))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(dailyCostValues.size()), 4, RoundingMode.HALF_UP);

        BigDecimal stdDev = variance.sqrt(MathContext.DECIMAL64);

        // Detect anomalies (costs > mean + 2*stdDev)
        BigDecimal threshold = mean.add(stdDev.multiply(BigDecimal.valueOf(2)));

        List<CostAnomaly> anomalies = new ArrayList<>();
        for (Map.Entry<LocalDateTime, BigDecimal> entry : dailyCosts.entrySet()) {
            if (entry.getValue().compareTo(threshold) > 0) {
                BigDecimal deviation = entry.getValue().subtract(mean)
                    .divide(mean, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

                anomalies.add(new CostAnomaly(
                    entry.getKey(),
                    entry.getValue(),
                    mean,
                    deviation,
                    "Daily cost exceeded normal range"
                ));
            }
        }

        return anomalies.stream()
            .sorted((a, b) -> b.getCost().compareTo(a.getCost()))
            .collect(Collectors.toList());
    }

    @Transactional
    public void setCostAlert(String alertType, BigDecimal threshold, String notificationEmail) {
        CostAlert alert = new CostAlert();
        alert.setAlertType(alertType);
        alert.setThreshold(threshold);
        alert.setNotificationEmail(notificationEmail);
        alert.setEnabled(true);
        alert.setCreatedAt(LocalDateTime.now());

        costAlertRepository.save(alert);
        logger.info("Created cost alert: {} threshold ${}", alertType, threshold);
    }

    private void checkCostAlerts(ModelUsage usage) {
        List<CostAlert> activeAlerts = costAlertRepository.findByEnabled(true);

        for (CostAlert alert : activeAlerts) {
            boolean shouldTrigger = checkAlertCondition(alert, usage);
            if (shouldTrigger) {
                triggerCostAlert(alert, usage);
            }
        }
    }

    private boolean checkAlertCondition(CostAlert alert, ModelUsage usage) {
        return switch (alert.getAlertType()) {
            case "DAILY_COST" -> {
                // Check if daily cost exceeds threshold
                LocalDateTime today = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
                BigDecimal dailyCost = modelUsageRepository.findByTimestampBetween(today, today.plusDays(1))
                    .stream()
                    .map(ModelUsage::getCost)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                dailyCost.compareTo(alert.getThreshold()) > 0;
            }
            case "USER_COST" -> {
                // Check if single user exceeds threshold in last 24 hours
                LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
                BigDecimal userCost = modelUsageRepository.findByUserIdAndTimestampBetween(usage.getUserId(), yesterday, LocalDateTime.now())
                    .stream()
                    .map(ModelUsage::getCost)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                userCost.compareTo(alert.getThreshold()) > 0;
            }
            case "MODEL_COST" -> {
                // Check if model cost exceeds threshold
                BigDecimal modelCost = modelUsageRepository.findByModelTypeAndTimestampBetween(usage.getModelType(), LocalDateTime.now().minusDays(1), LocalDateTime.now())
                    .stream()
                    .map(ModelUsage::getCost)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                modelCost.compareTo(alert.getThreshold()) > 0;
            }
            default -> false;
        };
    }

    private void triggerCostAlert(CostAlert alert, ModelUsage usage) {
        // In a real implementation, this would send email/SMS notifications
        logger.warn("COST ALERT TRIGGERED: {} exceeded threshold ${}. Triggered by usage: {}",
                   alert.getAlertType(), alert.getThreshold(), usage.getId());

        // Record alert trigger
        alert.setLastTriggeredAt(LocalDateTime.now());
        alert.setTriggerCount(alert.getTriggerCount() + 1);
        costAlertRepository.save(alert);
    }

    @Transactional(readOnly = true)
    public CostBudgetAnalysis analyzeBudgetCompliance(BigDecimal monthlyBudget, LocalDateTime startDate) {
        LocalDateTime endDate = startDate.plusMonths(1);
        List<ModelUsage> usages = modelUsageRepository.findByTimestampBetween(startDate, endDate);

        BigDecimal currentSpend = usages.stream()
            .map(ModelUsage::getCost)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingBudget = monthlyBudget.subtract(currentSpend);
        double budgetUtilizationPercent = currentSpend.divide(monthlyBudget, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .doubleValue();

        // Project end-of-month spend
        long daysPassed = ChronoUnit.DAYS.between(startDate, LocalDateTime.now()) + 1;
        long totalDaysInMonth = endDate.toLocalDate().lengthOfMonth();
        BigDecimal projectedSpend = currentSpend.divide(BigDecimal.valueOf(daysPassed), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(totalDaysInMonth));

        boolean overBudget = projectedSpend.compareTo(monthlyBudget) > 0;

        return new CostBudgetAnalysis(
            monthlyBudget,
            currentSpend,
            remainingBudget,
            projectedSpend,
            budgetUtilizationPercent,
            overBudget,
            LocalDateTime.now()
        );
    }

    // Utility method to calculate cost for given usage
    public BigDecimal calculateCost(String modelType, long inputTokens, long outputTokens) {
        BigDecimal rate = modelCostRates.getOrDefault(modelType, BigDecimal.ZERO);

        // Different pricing for input vs output tokens in some models
        BigDecimal inputCost = BigDecimal.valueOf(inputTokens)
            .multiply(rate)
            .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);

        BigDecimal outputCost = BigDecimal.valueOf(outputTokens)
            .multiply(rate)
            .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);

        return inputCost.add(outputCost);
    }

    @Transactional(readOnly = true)
    public Page<ModelUsage> getUsageHistory(Pageable pageable) {
        return modelUsageRepository.findAllByOrderByTimestampDesc(pageable);
    }
}
