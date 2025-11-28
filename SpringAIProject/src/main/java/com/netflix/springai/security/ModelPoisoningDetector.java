package com.netflix.springai.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class ModelPoisoningDetector {

    private static final Logger logger = LoggerFactory.getLogger(ModelPoisoningDetector.class);

    // Poisoning detection patterns
    private static final List<Pattern> POISONING_PATTERNS = Arrays.asList(
        // Data poisoning patterns
        Pattern.compile("(?i)\\b(poison|corrupt|manipulate)\\s+(the|this|model|training)\\s+data\\b"),
        Pattern.compile("(?i)\\b(inject|insert|add)\\s+(malicious|bad|harmful)\\s+(data|examples)\\b"),
        Pattern.compile("(?i)\\b(alter|change|modify)\\s+(training|dataset|model)\\s+(to|so)\\s+(produce|generate|create)\\b"),

        // Backdoor insertion
        Pattern.compile("(?i)\\b(backdoor|trojan|hidden)\\s+(trigger|activation|pattern)\\b"),
        Pattern.compile("(?i)\\b(secret|hidden|undetectable)\\s+(command|instruction|behavior)\\b"),
        Pattern.compile("(?i)\\b(trigger|activate)\\s+(when|if)\\s+(you\\s+)?see\\s+\\[.*\\]\\b"),

        // Adversarial examples
        Pattern.compile("(?i)\\b(adversarial|perturbed|modified)\\s+(input|example|sample)\\b"),
        Pattern.compile("(?i)\\b(confuse|mislead|trick)\\s+(the|this)\\s+model\\b"),
        Pattern.compile("(?i)\\b(bypass|circumvent|evade)\\s+(detection|filter|security)\\b"),

        // Model inversion attacks
        Pattern.compile("(?i)\\b(reconstruct|recover|extract)\\s+(training|private|sensitive)\\s+data\\b"),
        Pattern.compile("(?i)\\b(invert|reverse)\\s+(the|this)\\s+model\\b"),
        Pattern.compile("(?i)\\b(leak|disclose|reveal)\\s+(confidential|private|training)\\s+information\\b"),

        // Evasion techniques
        Pattern.compile("(?i)\\b(hide|mask|obfuscate)\\s+(malicious|bad|intention)\\b"),
        Pattern.compile("(?i)\\b(encode|encrypt|cipher)\\s+(attack|payload|instruction)\\b"),
        Pattern.compile("(?i)\\b(steganography|watermark|invisible)\\s+(message|data|content)\\b")
    );

    // Suspicious behavioral patterns
    private static final Set<String> SUSPICIOUS_BEHAVIORS = Set.of(
        "unusual_output_distribution", "sudden_performance_drop", "anomalous_token_usage",
        "unexpected_confidence_scores", "repeated_similar_requests", "high_frequency_requests"
    );

    // Model baseline metrics for anomaly detection
    private final Map<String, ModelBaseline> modelBaselines = new ConcurrentHashMap<>();

    // Recent poisoning attempts
    private final Map<String, PoisoningAttempt> recentAttempts = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, PoisoningAttempt> eldest) {
            return size() > 500; // Keep last 500 attempts
        }
    };

    public PoisoningAnalysis analyzeRequest(String modelType, String input, String output,
                                          Map<String, Object> metadata, String userId) {

        long startTime = System.nanoTime();

        // Multi-layer analysis
        List<String> detectedPatterns = detectPoisoningPatterns(input, output);
        BehavioralAnomalies anomalies = detectBehavioralAnomalies(modelType, input, output, metadata);
        double riskScore = calculatePoisoningRisk(detectedPatterns, anomalies, metadata);

        PoisoningThreatLevel threatLevel = determineThreatLevel(riskScore, detectedPatterns.size(), anomalies);

        List<String> mitigationStrategies = generateMitigationStrategies(threatLevel, detectedPatterns, anomalies);

        PoisoningAnalysis analysis = new PoisoningAnalysis(
            threatLevel,
            riskScore,
            detectedPatterns,
            anomalies,
            mitigationStrategies,
            System.nanoTime() - startTime,
            LocalDateTime.now()
        );

        // Log significant threats
        if (threatLevel != PoisoningThreatLevel.SAFE && threatLevel != PoisoningThreatLevel.LOW) {
            logPoisoningAttempt(modelType, userId, input, analysis);
        }

        return analysis;
    }

    public ModelIntegrityAssessment assessModelIntegrity(String modelType, List<ModelMetrics> recentMetrics) {
        ModelBaseline baseline = modelBaselines.computeIfAbsent(modelType, k -> new ModelBaseline());

        // Update baseline with recent metrics
        baseline.updateBaseline(recentMetrics);

        // Analyze for poisoning indicators
        List<String> integrityIssues = new ArrayList<>();

        // Check for sudden performance degradation
        if (baseline.hasPerformanceDegradation(recentMetrics)) {
            integrityIssues.add("PERFORMANCE_DEGRADATION");
        }

        // Check for anomalous output distributions
        if (baseline.hasAnomalousOutputs(recentMetrics)) {
            integrityIssues.add("ANOMALOUS_OUTPUT_DISTRIBUTION");
        }

        // Check for unusual token usage patterns
        if (baseline.hasUnusualTokenUsage(recentMetrics)) {
            integrityIssues.add("UNUSUAL_TOKEN_USAGE");
        }

        // Check for confidence score anomalies
        if (baseline.hasConfidenceAnomalies(recentMetrics)) {
            integrityIssues.add("CONFIDENCE_SCORE_ANOMALIES");
        }

        double integrityScore = calculateIntegrityScore(integrityIssues, recentMetrics);

        ModelIntegrityStatus status = determineIntegrityStatus(integrityScore, integrityIssues);

        return new ModelIntegrityAssessment(
            modelType,
            status,
            integrityScore,
            integrityIssues,
            baseline.getLastUpdated(),
            LocalDateTime.now()
        );
    }

    public void updateModelBaseline(String modelType, ModelMetrics metrics) {
        ModelBaseline baseline = modelBaselines.computeIfAbsent(modelType, k -> new ModelBaseline());
        baseline.updateWithMetrics(metrics);
    }

    public boolean shouldQuarantineModel(String modelType, ModelIntegrityAssessment assessment) {
        return assessment.getStatus() == ModelIntegrityStatus.COMPROMISED ||
               assessment.getStatus() == ModelIntegrityStatus.HIGH_RISK;
    }

    public List<String> getActivePoisoningCampaigns() {
        // Analyze recent attempts for patterns that suggest coordinated attacks
        Map<String, List<PoisoningAttempt>> attemptsByUser = recentAttempts.values().stream()
            .collect(Collectors.groupingBy(PoisoningAttempt::getUserId));

        List<String> campaigns = new ArrayList<>();

        for (Map.Entry<String, List<PoisoningAttempt>> entry : attemptsByUser.entrySet()) {
            List<PoisoningAttempt> userAttempts = entry.getValue();

            // Check for rapid succession attacks
            if (userAttempts.size() >= 5) {
                long timeSpan = userAttempts.get(userAttempts.size() - 1).getTimestamp() -
                               userAttempts.get(0).getTimestamp();

                if (timeSpan < 3600000) { // Within 1 hour
                    campaigns.add("RAPID_ATTACKS_USER_" + entry.getKey());
                }
            }

            // Check for pattern-based attacks
            long highThreatCount = userAttempts.stream()
                .mapToLong(attempt -> attempt.getAnalysis().getThreatLevel().ordinal())
                .filter(level -> level >= PoisoningThreatLevel.MEDIUM.ordinal())
                .count();

            if (highThreatCount >= 3) {
                campaigns.add("PATTERN_ATTACKS_USER_" + entry.getKey());
            }
        }

        return campaigns;
    }

    private List<String> detectPoisoningPatterns(String input, String output) {
        List<String> detected = new ArrayList<>();

        // Check input for poisoning patterns
        if (input != null) {
            for (Pattern pattern : POISONING_PATTERNS) {
                if (pattern.matcher(input).find()) {
                    detected.add("INPUT:" + pattern.pattern());
                }
            }
        }

        // Check output for poisoning indicators (unusual responses)
        if (output != null) {
            // Check for encoded/hidden content
            if (containsEncodedContent(output)) {
                detected.add("OUTPUT:ENCODED_CONTENT");
            }

            // Check for unusual confidence markers
            if (containsConfidenceMarkers(output)) {
                detected.add("OUTPUT:CONFIDENCE_MARKERS");
            }
        }

        return detected.stream().distinct().collect(Collectors.toList());
    }

    private BehavioralAnomalies detectBehavioralAnomalies(String modelType, String input, String output,
                                                         Map<String, Object> metadata) {

        List<String> anomalies = new ArrayList<>();
        Map<String, Double> anomalyScores = new HashMap<>();

        ModelBaseline baseline = modelBaselines.get(modelType);
        if (baseline == null) {
            return new BehavioralAnomalies(Collections.emptyList(), Collections.emptyMap());
        }

        // Analyze token usage
        Integer inputTokens = (Integer) metadata.getOrDefault("input_tokens", 0);
        Integer outputTokens = (Integer) metadata.getOrDefault("output_tokens", 0);

        double tokenUsageRatio = baseline.calculateTokenUsageAnomaly(inputTokens, outputTokens);
        if (tokenUsageRatio > 2.0) {
            anomalies.add("HIGH_TOKEN_USAGE_RATIO");
            anomalyScores.put("TOKEN_USAGE", tokenUsageRatio);
        }

        // Analyze response time
        Long responseTime = (Long) metadata.getOrDefault("response_time_ms", 0L);
        double responseTimeAnomaly = baseline.calculateResponseTimeAnomaly(responseTime);
        if (responseTimeAnomaly > 1.5) {
            anomalies.add("SLOW_RESPONSE_TIME");
            anomalyScores.put("RESPONSE_TIME", responseTimeAnomaly);
        }

        // Analyze request frequency
        String userId = (String) metadata.getOrDefault("user_id", "unknown");
        double frequencyAnomaly = calculateRequestFrequencyAnomaly(userId);
        if (frequencyAnomaly > 2.0) {
            anomalies.add("HIGH_REQUEST_FREQUENCY");
            anomalyScores.put("FREQUENCY", frequencyAnomaly);
        }

        // Analyze content patterns
        if (input != null && baseline.detectsContentAnomaly(input)) {
            anomalies.add("UNUSUAL_CONTENT_PATTERN");
            anomalyScores.put("CONTENT", 2.5);
        }

        return new BehavioralAnomalies(anomalies, anomalyScores);
    }

    private double calculatePoisoningRisk(List<String> patterns, BehavioralAnomalies anomalies,
                                        Map<String, Object> metadata) {

        double riskScore = 0.0;

        // Pattern-based risk
        riskScore += patterns.size() * 20.0;

        // Anomaly-based risk
        riskScore += anomalies.getAnomalies().size() * 15.0;
        riskScore += anomalies.getScores().values().stream().mapToDouble(Double::doubleValue).sum() * 5.0;

        // Contextual risk
        String userId = (String) metadata.getOrDefault("user_id", "");
        if (!userId.isEmpty()) {
            long userAttemptCount = recentAttempts.values().stream()
                .filter(attempt -> attempt.getUserId().equals(userId))
                .count();
            riskScore += Math.min(userAttemptCount * 10.0, 50.0); // Cap at 50
        }

        // Time-based risk (attacks during off-hours might be automated)
        int hourOfDay = LocalDateTime.now().getHour();
        if (hourOfDay < 6 || hourOfDay > 22) {
            riskScore += 10.0;
        }

        return Math.min(riskScore, 100.0);
    }

    private PoisoningThreatLevel determineThreatLevel(double riskScore, int patternCount,
                                                     BehavioralAnomalies anomalies) {

        int anomalyCount = anomalies.getAnomalies().size();

        if (riskScore >= 80.0 || (patternCount >= 2 && anomalyCount >= 2)) {
            return PoisoningThreatLevel.CRITICAL;
        } else if (riskScore >= 60.0 || patternCount >= 1 || anomalyCount >= 3) {
            return PoisoningThreatLevel.HIGH;
        } else if (riskScore >= 40.0 || anomalyCount >= 2) {
            return PoisoningThreatLevel.MEDIUM;
        } else if (riskScore >= 20.0 || anomalyCount >= 1) {
            return PoisoningThreatLevel.LOW;
        } else {
            return PoisoningThreatLevel.SAFE;
        }
    }

    private List<String> generateMitigationStrategies(PoisoningThreatLevel threatLevel,
                                                    List<String> patterns, BehavioralAnomalies anomalies) {

        List<String> strategies = new ArrayList<>();

        switch (threatLevel) {
            case CRITICAL:
                strategies.add("IMMEDIATE_QUARANTINE: Isolate affected model");
                strategies.add("SECURITY_ALERT: Notify security team");
                strategies.add("REQUEST_BLOCK: Block suspicious user");
                strategies.add("MODEL_ROLLBACK: Roll back to clean checkpoint");
                break;
            case HIGH:
                strategies.add("ENHANCED_MONITORING: Increase monitoring for affected model");
                strategies.add("USER_RESTRICTION: Temporarily limit user capabilities");
                strategies.add("PATTERN_ANALYSIS: Deep analysis of detected patterns");
                break;
            case MEDIUM:
                strategies.add("ADDITIONAL_VALIDATION: Extra validation for user requests");
                strategies.add("RESPONSE_FILTERING: Filter potentially poisoned responses");
                strategies.add("METRIC_MONITORING: Enhanced metrics collection");
                break;
            case LOW:
                strategies.add("LOGGING_INCREASE: Increase logging level");
                strategies.add("PATTERN_TRACKING: Track for emerging patterns");
                break;
            case SAFE:
                // No additional strategies needed
                break;
        }

        return strategies;
    }

    private void logPoisoningAttempt(String modelType, String userId, String input, PoisoningAnalysis analysis) {
        String attemptId = UUID.randomUUID().toString();
        PoisoningAttempt attempt = new PoisoningAttempt(
            attemptId,
            modelType,
            userId,
            input.substring(0, Math.min(input.length(), 200)), // Truncate for logging
            analysis,
            System.currentTimeMillis()
        );

        recentAttempts.put(attemptId, attempt);

        logger.warn("MODEL_POISONING_DETECTED: Model={}, User={}, ThreatLevel={}, RiskScore={}",
                   modelType, userId, analysis.getThreatLevel(), analysis.getRiskScore());
    }

    private boolean containsEncodedContent(String text) {
        // Check for base64, hex, or other encoded content patterns
        return text.matches(".*[A-Za-z0-9+/=]{20,}.*") || // Potential base64
               text.matches(".*\\\\x[0-9A-Fa-f]{2}.*") || // Hex encoding
               text.matches(".*[01]{8,}.*"); // Binary strings
    }

    private boolean containsConfidenceMarkers(String text) {
        // Check for unusual confidence expressions that might indicate manipulation
        return text.toLowerCase().matches(".*\\b(confidence|probability|certainty)\\s*[:=]\\s*[0-9.]+%?.*");
    }

    private double calculateRequestFrequencyAnomaly(String userId) {
        // Calculate requests per minute for the last hour
        long oneHourAgo = System.currentTimeMillis() - 3600000;
        long recentRequests = recentAttempts.values().stream()
            .filter(attempt -> attempt.getUserId().equals(userId))
            .filter(attempt -> attempt.getTimestamp() > oneHourAgo)
            .count();

        // Normal rate: ~10 requests per hour, anomaly if > 50
        return recentRequests / 5.0; // Normalize to 10 = normal
    }

    private double calculateIntegrityScore(List<String> issues, List<ModelMetrics> recentMetrics) {
        double baseScore = 100.0;

        // Deduct points for each issue
        baseScore -= issues.size() * 15.0;

        // Analyze metrics trend
        if (!recentMetrics.isEmpty()) {
            ModelMetrics latest = recentMetrics.get(recentMetrics.size() - 1);
            ModelMetrics previous = recentMetrics.size() > 1 ?
                recentMetrics.get(recentMetrics.size() - 2) : latest;

            // Performance degradation
            if (latest.getAccuracy() < previous.getAccuracy() * 0.95) {
                baseScore -= 10.0;
            }

            // Unusual confidence scores
            if (Math.abs(latest.getAvgConfidence() - previous.getAvgConfidence()) > 0.2) {
                baseScore -= 5.0;
            }
        }

        return Math.max(baseScore, 0.0);
    }

    private ModelIntegrityStatus determineIntegrityStatus(double score, List<String> issues) {
        if (score < 50.0 || issues.contains("PERFORMANCE_DEGRADATION")) {
            return ModelIntegrityStatus.COMPROMISED;
        } else if (score < 70.0 || !issues.isEmpty()) {
            return ModelIntegrityStatus.HIGH_RISK;
        } else if (score < 85.0) {
            return ModelIntegrityStatus.MEDIUM_RISK;
        } else if (score < 95.0) {
            return ModelIntegrityStatus.LOW_RISK;
        } else {
            return ModelIntegrityStatus.HEALTHY;
        }
    }

    // Enums and data classes
    public enum PoisoningThreatLevel {
        SAFE, LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum ModelIntegrityStatus {
        HEALTHY, LOW_RISK, MEDIUM_RISK, HIGH_RISK, COMPROMISED
    }

    public static class PoisoningAnalysis {
        private final PoisoningThreatLevel threatLevel;
        private final double riskScore;
        private final List<String> detectedPatterns;
        private final BehavioralAnomalies anomalies;
        private final List<String> mitigationStrategies;
        private final long processingTimeNs;
        private final LocalDateTime analyzedAt;

        public PoisoningAnalysis(PoisoningThreatLevel threatLevel, double riskScore, List<String> detectedPatterns,
                               BehavioralAnomalies anomalies, List<String> mitigationStrategies,
                               long processingTimeNs, LocalDateTime analyzedAt) {
            this.threatLevel = threatLevel;
            this.riskScore = riskScore;
            this.detectedPatterns = detectedPatterns;
            this.anomalies = anomalies;
            this.mitigationStrategies = mitigationStrategies;
            this.processingTimeNs = processingTimeNs;
            this.analyzedAt = analyzedAt;
        }

        // Getters
        public PoisoningThreatLevel getThreatLevel() { return threatLevel; }
        public double getRiskScore() { return riskScore; }
        public List<String> getDetectedPatterns() { return detectedPatterns; }
        public BehavioralAnomalies getAnomalies() { return anomalies; }
        public List<String> getMitigationStrategies() { return mitigationStrategies; }
        public long getProcessingTimeNs() { return processingTimeNs; }
        public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    }

    public static class BehavioralAnomalies {
        private final List<String> anomalies;
        private final Map<String, Double> scores;

        public BehavioralAnomalies(List<String> anomalies, Map<String, Double> scores) {
            this.anomalies = anomalies;
            this.scores = scores;
        }

        public List<String> getAnomalies() { return anomalies; }
        public Map<String, Double> getScores() { return scores; }
    }

    public static class ModelIntegrityAssessment {
        private final String modelType;
        private final ModelIntegrityStatus status;
        private final double integrityScore;
        private final List<String> issues;
        private final LocalDateTime baselineUpdatedAt;
        private final LocalDateTime assessedAt;

        public ModelIntegrityAssessment(String modelType, ModelIntegrityStatus status, double integrityScore,
                                      List<String> issues, LocalDateTime baselineUpdatedAt, LocalDateTime assessedAt) {
            this.modelType = modelType;
            this.status = status;
            this.integrityScore = integrityScore;
            this.issues = issues;
            this.baselineUpdatedAt = baselineUpdatedAt;
            this.assessedAt = assessedAt;
        }

        // Getters
        public String getModelType() { return modelType; }
        public ModelIntegrityStatus getStatus() { return status; }
        public double getIntegrityScore() { return integrityScore; }
        public List<String> getIssues() { return issues; }
        public LocalDateTime getBaselineUpdatedAt() { return baselineUpdatedAt; }
        public LocalDateTime getAssessedAt() { return assessedAt; }
    }

    public static class ModelMetrics {
        private final double accuracy;
        private final double avgConfidence;
        private final long totalRequests;
        private final LocalDateTime recordedAt;

        public ModelMetrics(double accuracy, double avgConfidence, long totalRequests, LocalDateTime recordedAt) {
            this.accuracy = accuracy;
            this.avgConfidence = avgConfidence;
            this.totalRequests = totalRequests;
            this.recordedAt = recordedAt;
        }

        // Getters
        public double getAccuracy() { return accuracy; }
        public double getAvgConfidence() { return avgConfidence; }
        public long getTotalRequests() { return totalRequests; }
        public LocalDateTime getRecordedAt() { return recordedAt; }
    }

    private static class ModelBaseline {
        private double avgAccuracy = 0.0;
        private double avgConfidence = 0.0;
        private double avgTokenRatio = 0.0;
        private long avgResponseTime = 0L;
        private int sampleCount = 0;
        private LocalDateTime lastUpdated = LocalDateTime.now();

        public synchronized void updateWithMetrics(ModelMetrics metrics) {
            sampleCount++;
            double weight = 1.0 / sampleCount;

            avgAccuracy = avgAccuracy * (1 - weight) + metrics.getAccuracy() * weight;
            avgConfidence = avgConfidence * (1 - weight) + metrics.getAvgConfidence() * weight;

            lastUpdated = LocalDateTime.now();
        }

        public synchronized void updateBaseline(List<ModelMetrics> recentMetrics) {
            if (recentMetrics.isEmpty()) return;

            // Use exponential moving average for baseline
            double alpha = 0.1; // Smoothing factor

            for (ModelMetrics metrics : recentMetrics) {
                avgAccuracy = alpha * metrics.getAccuracy() + (1 - alpha) * avgAccuracy;
                avgConfidence = alpha * metrics.getAvgConfidence() + (1 - alpha) * avgConfidence;
            }

            sampleCount = Math.max(sampleCount, recentMetrics.size());
            lastUpdated = LocalDateTime.now();
        }

        public double calculateTokenUsageAnomaly(int inputTokens, int outputTokens) {
            if (avgTokenRatio == 0.0) return 1.0;
            double currentRatio = (double) outputTokens / Math.max(inputTokens, 1);
            return Math.abs(currentRatio - avgTokenRatio) / avgTokenRatio;
        }

        public double calculateResponseTimeAnomaly(long responseTime) {
            if (avgResponseTime == 0L) return 1.0;
            return (double) responseTime / avgResponseTime;
        }

        public boolean hasPerformanceDegradation(List<ModelMetrics> recentMetrics) {
            if (recentMetrics.size() < 3) return false;

            ModelMetrics latest = recentMetrics.get(recentMetrics.size() - 1);
            double recentAvg = recentMetrics.subList(Math.max(0, recentMetrics.size() - 3), recentMetrics.size())
                .stream().mapToDouble(ModelMetrics::getAccuracy).average().orElse(0.0);

            return latest.getAccuracy() < recentAvg * 0.9; // 10% degradation
        }

        public boolean hasAnomalousOutputs(List<ModelMetrics> recentMetrics) {
            // Simplified check - in practice, use statistical analysis
            return recentMetrics.stream()
                .anyMatch(m -> Math.abs(m.getAvgConfidence() - avgConfidence) > avgConfidence * 0.5);
        }

        public boolean hasUnusualTokenUsage(List<ModelMetrics> recentMetrics) {
            // Placeholder - implement token usage anomaly detection
            return false;
        }

        public boolean hasConfidenceAnomalies(List<ModelMetrics> recentMetrics) {
            double stdDev = calculateConfidenceStdDev(recentMetrics);
            return stdDev > avgConfidence * 0.3; // High variability
        }

        private double calculateConfidenceStdDev(List<ModelMetrics> metrics) {
            double mean = metrics.stream().mapToDouble(ModelMetrics::getAvgConfidence).average().orElse(0.0);
            double variance = metrics.stream()
                .mapToDouble(m -> Math.pow(m.getAvgConfidence() - mean, 2))
                .average().orElse(0.0);
            return Math.sqrt(variance);
        }

        public boolean detectsContentAnomaly(String input) {
            // Placeholder - implement content pattern anomaly detection
            return false;
        }

        public LocalDateTime getLastUpdated() { return lastUpdated; }
    }

    private static class PoisoningAttempt {
        private final String id;
        private final String modelType;
        private final String userId;
        private final String inputSnippet;
        private final PoisoningAnalysis analysis;
        private final long timestamp;

        public PoisoningAttempt(String id, String modelType, String userId, String inputSnippet,
                              PoisoningAnalysis analysis, long timestamp) {
            this.id = id;
            this.modelType = modelType;
            this.userId = userId;
            this.inputSnippet = inputSnippet;
            this.analysis = analysis;
            this.timestamp = timestamp;
        }

        // Getters
        public String getId() { return id; }
        public String getModelType() { return modelType; }
        public String getUserId() { return userId; }
        public String getInputSnippet() { return inputSnippet; }
        public PoisoningAnalysis getAnalysis() { return analysis; }
        public long getTimestamp() { return timestamp; }
    }
}
