package com.netflix.springai.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class ModelABTestingService {

    private static final Logger logger = LoggerFactory.getLogger(ModelABTestingService.class);

    private final ABTestRepository abTestRepository;
    private final ABTestResultRepository abTestResultRepository;
    private final ModelVersionManager modelVersionManager;

    // Active A/B tests cache
    private final Map<String, ABTest> activeTests = new ConcurrentHashMap<>();

    @Autowired
    public ModelABTestingService(
            ABTestRepository abTestRepository,
            ABTestResultRepository abTestResultRepository,
            ModelVersionManager modelVersionManager) {
        this.abTestRepository = abTestRepository;
        this.abTestResultRepository = abTestResultRepository;
        this.modelVersionManager = modelVersionManager;

        // Load active tests on startup
        loadActiveTests();
    }

    @Transactional
    public ABTest createABTest(ABTest abTest) {
        logger.info("Creating A/B test: {}", abTest.getName());

        // Validate test configuration
        validateABTestConfiguration(abTest);

        // Set creation metadata
        abTest.setCreatedAt(LocalDateTime.now());
        abTest.setStatus(ABTestStatus.DRAFT);

        ABTest savedTest = abTestRepository.save(abTest);
        logger.info("Created A/B test with ID: {}", savedTest.getId());

        return savedTest;
    }

    @Transactional
    public ABTest startABTest(Long testId) {
        logger.info("Starting A/B test with ID: {}", testId);

        ABTest abTest = abTestRepository.findById(testId)
            .orElseThrow(() -> new IllegalArgumentException("A/B test not found"));

        if (abTest.getStatus() != ABTestStatus.DRAFT) {
            throw new IllegalStateException("Can only start tests in DRAFT status");
        }

        // Validate all model versions exist and are active
        for (ABTestVariant variant : abTest.getVariants()) {
            AIModelVersion modelVersion = modelVersionManager.getActiveModelVersion(variant.getModelType())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Model type not found: " + variant.getModelType()));
        }

        abTest.setStatus(ABTestStatus.RUNNING);
        abTest.setStartedAt(LocalDateTime.now());

        ABTest startedTest = abTestRepository.save(abTest);
        activeTests.put(abTest.getTestKey(), startedTest);

        logger.info("Started A/B test: {}", abTest.getName());
        return startedTest;
    }

    @Transactional
    public ABTest stopABTest(Long testId) {
        logger.info("Stopping A/B test with ID: {}", testId);

        ABTest abTest = abTestRepository.findById(testId)
            .orElseThrow(() -> new IllegalArgumentException("A/B test not found"));

        if (abTest.getStatus() != ABTestStatus.RUNNING) {
            throw new IllegalStateException("Can only stop running tests");
        }

        abTest.setStatus(ABTestStatus.COMPLETED);
        abTest.setEndedAt(LocalDateTime.now());

        ABTest stoppedTest = abTestRepository.save(abTest);
        activeTests.remove(abTest.getTestKey());

        // Generate final results
        generateFinalResults(stoppedTest);

        logger.info("Stopped A/B test: {}", abTest.getName());
        return stoppedTest;
    }

    public AIModelVersion selectModelForRequest(String userId, String modelType, String testKey) {
        ABTest abTest = activeTests.get(testKey);
        if (abTest == null) {
            // No active test, return current active model
            return modelVersionManager.getActiveModelVersion(modelType)
                .orElseThrow(() -> new IllegalArgumentException("No active model found for type: " + modelType));
        }

        // Check if user is already assigned to a variant
        String userKey = generateUserKey(userId, testKey);
        ABTestResult existingResult = abTestResultRepository.findByTestAndUserKey(abTest, userKey);

        if (existingResult != null) {
            // Return previously assigned model
            return existingResult.getAssignedVariant().getModelVersion();
        }

        // Assign user to a variant based on traffic distribution
        ABTestVariant assignedVariant = assignUserToVariant(abTest, userId);

        // Record the assignment
        recordUserAssignment(abTest, assignedVariant, userKey);

        return assignedVariant.getModelVersion();
    }

    private ABTestVariant assignUserToVariant(ABTest abTest, String userId) {
        List<ABTestVariant> variants = abTest.getVariants();
        double randomValue = ThreadLocalRandom.current().nextDouble();

        double cumulativeWeight = 0.0;
        for (ABTestVariant variant : variants) {
            cumulativeWeight += variant.getTrafficWeight();
            if (randomValue <= cumulativeWeight) {
                return variant;
            }
        }

        // Fallback to first variant
        return variants.get(0);
    }

    private void recordUserAssignment(ABTest abTest, ABTestVariant variant, String userKey) {
        ABTestResult result = new ABTestResult();
        result.setAbTest(abTest);
        result.setAssignedVariant(variant);
        result.setUserKey(userKey);
        result.setAssignedAt(LocalDateTime.now());

        abTestResultRepository.save(result);
    }

    @Transactional
    public void recordTestResult(String userId, String testKey, String modelType, String modelVersion,
                                ModelPerformanceMetrics metrics, boolean conversion) {
        ABTest abTest = activeTests.get(testKey);
        if (abTest == null) return;

        String userKey = generateUserKey(userId, testKey);
        ABTestResult result = abTestResultRepository.findByTestAndUserKey(abTest, userKey);

        if (result == null) return;

        // Update result with performance metrics
        result.setResponseTime(metrics.getAverageResponseTime());
        result.setTokenUsage(metrics.getTokenUsage());
        result.setCost(metrics.getCost());
        result.setConversion(conversion);
        result.setCompletedAt(LocalDateTime.now());

        abTestResultRepository.save(result);
    }

    @Transactional(readOnly = true)
    public ABTestResults getTestResults(Long testId) {
        ABTest abTest = abTestRepository.findById(testId)
            .orElseThrow(() -> new IllegalArgumentException("A/B test not found"));

        List<ABTestResult> results = abTestResultRepository.findByAbTest(abTest);

        return calculateTestResults(abTest, results);
    }

    private ABTestResults calculateTestResults(ABTest abTest, List<ABTestResult> results) {
        Map<String, ABTestVariantResults> variantResults = new HashMap<>();

        for (ABTestVariant variant : abTest.getVariants()) {
            List<ABTestResult> variantData = results.stream()
                .filter(r -> r.getAssignedVariant().getId().equals(variant.getId()))
                .collect(Collectors.toList());

            ABTestVariantResults resultsData = calculateVariantResults(variant, variantData);
            variantResults.put(variant.getName(), resultsData);
        }

        // Calculate statistical significance
        ABTestVariantResults controlResults = variantResults.get(abTest.getControlVariantName());
        Map<String, StatisticalSignificance> significance = new HashMap<>();

        if (controlResults != null) {
            for (Map.Entry<String, ABTestVariantResults> entry : variantResults.entrySet()) {
                if (!entry.getKey().equals(abTest.getControlVariantName())) {
                    significance.put(entry.getKey(),
                        calculateStatisticalSignificance(controlResults, entry.getValue()));
                }
            }
        }

        return new ABTestResults(
            abTest.getId(),
            abTest.getName(),
            variantResults,
            significance,
            LocalDateTime.now()
        );
    }

    private ABTestVariantResults calculateVariantResults(ABTestVariant variant, List<ABTestResult> results) {
        if (results.isEmpty()) {
            return new ABTestVariantResults(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }

        long totalRequests = results.size();
        long conversions = results.stream().mapToLong(r -> r.isConversion() ? 1L : 0L).sum();

        double conversionRate = (double) conversions / totalRequests;
        double avgResponseTime = results.stream()
            .filter(r -> r.getResponseTime() != null)
            .mapToDouble(ABTestResult::getResponseTime)
            .average().orElse(0.0);

        double avgTokenUsage = results.stream()
            .filter(r -> r.getTokenUsage() != null)
            .mapToDouble(ABTestResult::getTokenUsage)
            .average().orElse(0.0);

        double avgCost = results.stream()
            .filter(r -> r.getCost() != null)
            .mapToDouble(ABTestResult::getCost)
            .average().orElse(0.0);

        // Calculate confidence interval for conversion rate
        double standardError = Math.sqrt(conversionRate * (1 - conversionRate) / totalRequests);
        double confidenceInterval = 1.96 * standardError; // 95% confidence

        return new ABTestVariantResults(
            totalRequests,
            conversionRate,
            confidenceInterval,
            avgResponseTime,
            avgTokenUsage,
            avgCost,
            conversions * 1.0
        );
    }

    private StatisticalSignificance calculateStatisticalSignificance(
            ABTestVariantResults control, ABTestVariantResults variant) {

        // Simplified statistical significance calculation (chi-square test)
        double controlConversions = control.getConversionCount();
        double variantConversions = variant.getConversionCount();
        double controlTotal = control.getTotalRequests();
        double variantTotal = variant.getTotalRequests();

        // Calculate chi-square statistic
        double totalConversions = controlConversions + variantConversions;
        double totalRequests = controlTotal + variantTotal;

        double expectedControlConversions = (controlTotal / totalRequests) * totalConversions;
        double expectedVariantConversions = (variantTotal / totalRequests) * totalConversions;

        double chiSquare = Math.pow(controlConversions - expectedControlConversions, 2) / expectedControlConversions +
                           Math.pow(variantConversions - expectedVariantConversions, 2) / expectedVariantConversions;

        // Chi-square critical value for 95% confidence (df=1) is approximately 3.84
        boolean isSignificant = chiSquare > 3.84;

        double relativeImprovement = ((variant.getConversionRate() - control.getConversionRate()) /
                                     control.getConversionRate()) * 100;

        return new StatisticalSignificance(isSignificant, chiSquare, relativeImprovement);
    }

    @Transactional(readOnly = true)
    public Page<ABTest> getABTests(Pageable pageable) {
        return abTestRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Transactional(readOnly = true)
    public List<ABTest> getActiveABTests() {
        return new ArrayList<>(activeTests.values());
    }

    private void validateABTestConfiguration(ABTest abTest) {
        if (abTest.getVariants().size() < 2) {
            throw new IllegalArgumentException("A/B test must have at least 2 variants");
        }

        double totalWeight = abTest.getVariants().stream()
            .mapToDouble(ABTestVariant::getTrafficWeight)
            .sum();

        if (Math.abs(totalWeight - 1.0) > 0.001) {
            throw new IllegalArgumentException("Traffic weights must sum to 1.0");
        }

        // Validate control variant exists
        boolean hasControl = abTest.getVariants().stream()
            .anyMatch(v -> v.getName().equals(abTest.getControlVariantName()));

        if (!hasControl) {
            throw new IllegalArgumentException("Control variant must be specified");
        }

        // Check for duplicate variant names
        Set<String> variantNames = abTest.getVariants().stream()
            .map(ABTestVariant::getName)
            .collect(Collectors.toSet());

        if (variantNames.size() != abTest.getVariants().size()) {
            throw new IllegalArgumentException("Variant names must be unique");
        }
    }

    private void generateFinalResults(ABTest abTest) {
        ABTestResults results = getTestResults(abTest.getId());

        // Determine winner based on statistical significance and conversion rate
        ABTestVariant winner = determineTestWinner(abTest, results);

        if (winner != null) {
            abTest.setWinnerVariant(winner);
            abTestRepository.save(abTest);

            logger.info("A/B test {} completed. Winner: {}", abTest.getName(), winner.getName());
        }
    }

    private ABTestVariant determineTestWinner(ABTest abTest, ABTestResults results) {
        ABTestVariant controlVariant = abTest.getVariants().stream()
            .filter(v -> v.getName().equals(abTest.getControlVariantName()))
            .findFirst().orElse(null);

        if (controlVariant == null) return null;

        ABTestVariantResults controlResults = results.getVariantResults().get(abTest.getControlVariantName());

        ABTestVariant bestVariant = null;
        double bestImprovement = 0.0;

        for (ABTestVariant variant : abTest.getVariants()) {
            if (variant.getName().equals(abTest.getControlVariantName())) continue;

            ABTestVariantResults variantResults = results.getVariantResults().get(variant.getName());
            StatisticalSignificance significance = results.getStatisticalSignificance().get(variant.getName());

            if (significance != null && significance.isSignificant() &&
                variantResults.getConversionRate() > controlResults.getConversionRate()) {

                double improvement = significance.getRelativeImprovement();
                if (improvement > bestImprovement) {
                    bestImprovement = improvement;
                    bestVariant = variant;
                }
            }
        }

        return bestVariant;
    }

    private String generateUserKey(String userId, String testKey) {
        // Create deterministic but distributed user key for consistent assignment
        return userId + "_" + testKey;
    }

    private void loadActiveTests() {
        List<ABTest> runningTests = abTestRepository.findByStatus(ABTestStatus.RUNNING);
        for (ABTest test : runningTests) {
            activeTests.put(test.getTestKey(), test);
        }

        logger.info("Loaded {} active A/B tests", activeTests.size());
    }
}
