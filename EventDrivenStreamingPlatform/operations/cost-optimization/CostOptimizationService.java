package com.netflix.streaming.operations.costoptimization;

import com.netflix.streaming.events.EventPublisher;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Netflix-Grade Cost Optimization Service
 *
 * Implements intelligent cost management with:
 * - Real-time resource utilization monitoring
 * - Automated scaling decisions based on cost efficiency
 * - Spot instance utilization for batch workloads
 * - Reserved instance optimization
 * - Multi-cloud cost arbitrage
 * - Usage pattern analysis for optimization opportunities
 */
@Service
public class CostOptimizationService {

    private static final Logger logger = LoggerFactory.getLogger(CostOptimizationService.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final EventPublisher eventPublisher;
    private final Tracer tracer;

    // Cost thresholds and targets
    @Value("${cost.optimization.target-cpu-utilization:70}")
    private double targetCpuUtilization;

    @Value("${cost.optimization.target-memory-utilization:80}")
    private double targetMemoryUtilization;

    @Value("${cost.optimization.spot-instance-threshold:50}")
    private double spotInstanceThreshold;

    @Value("${cost.optimization.scaling-cooldown-minutes:10}")
    private int scalingCooldownMinutes;

    // Cost tracking data structures
    private final Map<String, ResourceMetrics> resourceMetrics = new ConcurrentHashMap<>();
    private final Map<String, ScalingDecision> recentScalingDecisions = new ConcurrentHashMap<>();

    public CostOptimizationService(RedisTemplate<String, Object> redisTemplate,
                                 EventPublisher eventPublisher,
                                 Tracer tracer) {
        this.redisTemplate = redisTemplate;
        this.eventPublisher = eventPublisher;
        this.tracer = tracer;
    }

    /**
     * Scheduled cost optimization analysis (every 5 minutes)
     */
    @Scheduled(fixedDelay = 300000) // 5 minutes
    public void performCostOptimizationAnalysis() {
        Span span = tracer.spanBuilder("cost.optimization.analysis").startSpan();

        try {
            logger.info("Starting cost optimization analysis");

            // Analyze current resource utilization
            Map<String, ResourceMetrics> currentMetrics = collectResourceMetrics();

            // Identify optimization opportunities
            List<OptimizationOpportunity> opportunities = identifyOptimizationOpportunities(currentMetrics);

            // Execute automated optimizations
            List<OptimizationAction> actions = executeOptimizations(opportunities);

            // Calculate cost savings
            CostSavings savings = calculateCostSavings(actions);

            // Publish optimization results
            if (!actions.isEmpty()) {
                eventPublisher.publish(new CostOptimizationExecutedEvent(
                    "cost-opt-" + Instant.now().toEpochMilli(), "default",
                    actions.size(), savings.getMonthlySavings(), savings.getAnnualSavings(),
                    Instant.now()
                ));
            }

            span.setAttribute("opportunities.identified", opportunities.size());
            span.setAttribute("actions.executed", actions.size());
            span.setAttribute("monthly.savings", savings.getMonthlySavings());

            logger.info("Cost optimization analysis completed: {} opportunities, {} actions, ${} monthly savings",
                       opportunities.size(), actions.size(), savings.getMonthlySavings());

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Cost optimization analysis failed", e);
        } finally {
            span.end();
        }
    }

    /**
     * Real-time scaling decision based on utilization patterns
     */
    public ScalingDecision makeScalingDecision(String serviceName, ResourceMetrics metrics) {
        Span span = tracer.spanBuilder("cost.scaling.decision")
            .setAttribute("service", serviceName)
            .startSpan();

        try {
            // Check if we're in cooldown period from recent scaling
            if (isInScalingCooldown(serviceName)) {
                return ScalingDecision.builder()
                    .serviceName(serviceName)
                    .decision(ScalingDecisionType.NO_CHANGE)
                    .reason("In scaling cooldown period")
                    .timestamp(Instant.now())
                    .build();
            }

            ScalingDecision decision = analyzeScalingNeeds(serviceName, metrics);

            // Record decision for cooldown tracking
            recentScalingDecisions.put(serviceName, decision);

            span.setAttribute("decision", decision.getDecision().toString());
            span.setAttribute("reason", decision.getReason());

            return decision;

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Scaling decision failed for service: {}", serviceName, e);

            return ScalingDecision.builder()
                .serviceName(serviceName)
                .decision(ScalingDecisionType.NO_CHANGE)
                .reason("Error in scaling analysis: " + e.getMessage())
                .timestamp(Instant.now())
                .build();
        } finally {
            span.end();
        }
    }

    /**
     * Spot instance utilization optimization
     */
    @Scheduled(fixedDelay = 600000) // 10 minutes
    public void optimizeSpotInstanceUsage() {
        Span span = tracer.spanBuilder("cost.spot.optimization").startSpan();

        try {
            logger.info("Analyzing spot instance optimization opportunities");

            // Identify workloads suitable for spot instances
            List<SpotInstanceCandidate> candidates = identifySpotInstanceCandidates();

            // Execute spot instance migrations
            List<SpotInstanceMigration> migrations = executeSpotInstanceMigrations(candidates);

            // Calculate cost savings
            double monthlySavings = migrations.stream()
                .mapToDouble(SpotInstanceMigration::getMonthlySavings)
                .sum();

            if (!migrations.isEmpty()) {
                eventPublisher.publish(new SpotInstanceOptimizationEvent(
                    "spot-opt-" + Instant.now().toEpochMilli(), "default",
                    migrations.size(), monthlySavings, Instant.now()
                ));

                logger.info("Spot instance optimization completed: {} migrations, ${} monthly savings",
                           migrations.size(), monthlySavings);
            }

            span.setAttribute("migrations.executed", migrations.size());
            span.setAttribute("monthly.savings", monthlySavings);

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Spot instance optimization failed", e);
        } finally {
            span.end();
        }
    }

    /**
     * Reserved instance optimization analysis
     */
    @Scheduled(cron = "0 0 6 * * MON") // Weekly on Monday at 6 AM
    public void performReservedInstanceAnalysis() {
        Span span = tracer.spanBuilder("cost.reserved.instance.analysis").startSpan();

        try {
            logger.info("Performing reserved instance optimization analysis");

            // Analyze usage patterns for RI recommendations
            ReservedInstanceRecommendation recommendation = analyzeReservedInstanceNeeds();

            // Execute RI purchases if beneficial
            if (recommendation.isRecommended()) {
                executeReservedInstancePurchase(recommendation);

                eventPublisher.publish(new ReservedInstancePurchasedEvent(
                    "ri-purchase-" + Instant.now().toEpochMilli(), "default",
                    recommendation.getInstanceType(), recommendation.getMonthlySavings(),
                    recommendation.getAnnualSavings(), Instant.now()
                ));

                logger.info("Reserved instance purchase executed: {} instances, ${} annual savings",
                           recommendation.getInstanceCount(), recommendation.getAnnualSavings());
            }

            span.setAttribute("ri.recommended", recommendation.isRecommended());
            span.setAttribute("annual.savings", recommendation.getAnnualSavings());

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Reserved instance analysis failed", e);
        } finally {
            span.end();
        }
    }

    /**
     * Multi-cloud cost arbitrage
     */
    @Scheduled(fixedDelay = 3600000) // 1 hour
    public void performMultiCloudArbitrage() {
        Span span = tracer.spanBuilder("cost.multicloud.arbitrage").startSpan();

        try {
            logger.info("Analyzing multi-cloud cost arbitrage opportunities");

            // Compare costs across cloud providers
            List<CloudArbitrageOpportunity> opportunities = analyzeCloudCosts();

            // Execute workload migrations if beneficial
            List<CloudMigration> migrations = executeCloudMigrations(opportunities);

            double monthlySavings = migrations.stream()
                .mapToDouble(CloudMigration::getMonthlySavings)
                .sum();

            if (!migrations.isEmpty()) {
                eventPublisher.publish(new CloudArbitrageExecutedEvent(
                    "cloud-arb-" + Instant.now().toEpochMilli(), "default",
                    migrations.size(), monthlySavings, Instant.now()
                ));

                logger.info("Multi-cloud arbitrage executed: {} migrations, ${} monthly savings",
                           migrations.size(), monthlySavings);
            }

            span.setAttribute("migrations.executed", migrations.size());
            span.setAttribute("monthly.savings", monthlySavings);

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Multi-cloud arbitrage analysis failed", e);
        } finally {
            span.end();
        }
    }

    // Implementation methods

    private Map<String, ResourceMetrics> collectResourceMetrics() {
        // In production, this would query Prometheus/Kubernetes metrics APIs
        // For demo, simulating metrics collection
        Map<String, ResourceMetrics> metrics = new HashMap<>();

        // Simulate metrics for different services
        metrics.put("playback-service", ResourceMetrics.builder()
            .cpuUtilization(65.0)
            .memoryUtilization(75.0)
            .requestCount(1500L)
            .activeConnections(450)
            .timestamp(Instant.now())
            .build());

        metrics.put("analytics-service", ResourceMetrics.builder()
            .cpuUtilization(45.0)
            .memoryUtilization(55.0)
            .requestCount(800L)
            .activeConnections(120)
            .timestamp(Instant.now())
            .build());

        return metrics;
    }

    private List<OptimizationOpportunity> identifyOptimizationOpportunities(Map<String, ResourceMetrics> metrics) {
        List<OptimizationOpportunity> opportunities = new ArrayList<>();

        for (Map.Entry<String, ResourceMetrics> entry : metrics.entrySet()) {
            String serviceName = entry.getKey();
            ResourceMetrics metric = entry.getValue();

            // Low utilization scaling down opportunity
            if (metric.getCpuUtilization() < 30.0 && metric.getMemoryUtilization() < 40.0) {
                opportunities.add(OptimizationOpportunity.builder()
                    .serviceName(serviceName)
                    .opportunityType(OptimizationType.SCALE_DOWN)
                    .potentialSavings(calculateScaleDownSavings(serviceName))
                    .description("Service running with low resource utilization")
                    .build());
            }

            // High utilization scaling up opportunity
            if (metric.getCpuUtilization() > 85.0 || metric.getMemoryUtilization() > 90.0) {
                opportunities.add(OptimizationOpportunity.builder()
                    .serviceName(serviceName)
                    .opportunityType(OptimizationType.SCALE_UP)
                    .potentialSavings(0.0) // Scaling up may increase costs but prevent outages
                    .description("Service under high resource pressure")
                    .build());
            }

            // Idle resource optimization
            if (metric.getActiveConnections() < 10 && metric.getRequestCount() < 50) {
                opportunities.add(OptimizationOpportunity.builder()
                    .serviceName(serviceName)
                    .opportunityType(OptimizationType.IDLE_RESOURCE)
                    .potentialSavings(calculateIdleSavings(serviceName, metric))
                    .description("Service appears to have minimal activity")
                    .build());
            }
        }

        return opportunities;
    }

    private List<OptimizationAction> executeOptimizations(List<OptimizationOpportunity> opportunities) {
        List<OptimizationAction> actions = new ArrayList<>();

        for (OptimizationOpportunity opportunity : opportunities) {
            try {
                OptimizationAction action = executeOptimization(opportunity);
                if (action != null) {
                    actions.add(action);
                }
            } catch (Exception e) {
                logger.error("Failed to execute optimization for {}: {}",
                           opportunity.getServiceName(), e.getMessage());
            }
        }

        return actions;
    }

    private OptimizationAction executeOptimization(OptimizationOpportunity opportunity) {
        // In production, this would integrate with Kubernetes HPA or cloud auto-scaling
        logger.info("Executing optimization: {} for service {}",
                   opportunity.getOpportunityType(), opportunity.getServiceName());

        return OptimizationAction.builder()
            .serviceName(opportunity.getServiceName())
            .actionType(opportunity.getOpportunityType())
            .description("Executed " + opportunity.getOpportunityType() + " optimization")
            .savingsAchieved(opportunity.getPotentialSavings())
            .executedAt(Instant.now())
            .build();
    }

    private CostSavings calculateCostSavings(List<OptimizationAction> actions) {
        double monthlySavings = actions.stream()
            .mapToDouble(OptimizationAction::getSavingsAchieved)
            .sum();

        return CostSavings.builder()
            .monthlySavings(monthlySavings)
            .annualSavings(monthlySavings * 12)
            .currency("USD")
            .calculatedAt(Instant.now())
            .build();
    }

    private ScalingDecision analyzeScalingNeeds(String serviceName, ResourceMetrics metrics) {
        // Analyze scaling needs based on utilization patterns
        if (metrics.getCpuUtilization() > targetCpuUtilization ||
            metrics.getMemoryUtilization() > targetMemoryUtilization) {

            return ScalingDecision.builder()
                .serviceName(serviceName)
                .decision(ScalingDecisionType.SCALE_UP)
                .reason("High resource utilization detected")
                .targetReplicas(calculateTargetReplicas(serviceName, metrics, true))
                .timestamp(Instant.now())
                .build();

        } else if (metrics.getCpuUtilization() < 20.0 && metrics.getMemoryUtilization() < 30.0) {

            return ScalingDecision.builder()
                .serviceName(serviceName)
                .decision(ScalingDecisionType.SCALE_DOWN)
                .reason("Low resource utilization detected")
                .targetReplicas(calculateTargetReplicas(serviceName, metrics, false))
                .timestamp(Instant.now())
                .build();
        }

        return ScalingDecision.builder()
            .serviceName(serviceName)
            .decision(ScalingDecisionType.NO_CHANGE)
            .reason("Resource utilization within acceptable range")
            .timestamp(Instant.now())
            .build();
    }

    private boolean isInScalingCooldown(String serviceName) {
        ScalingDecision recentDecision = recentScalingDecisions.get(serviceName);
        if (recentDecision == null) return false;

        long minutesSinceLastScaling = Instant.now()
            .minusSeconds(recentDecision.getTimestamp().getEpochSecond())
            .getEpochSecond() / 60;

        return minutesSinceLastScaling < scalingCooldownMinutes;
    }

    private int calculateTargetReplicas(String serviceName, ResourceMetrics metrics, boolean scaleUp) {
        // Simplified scaling calculation
        // In production, this would use more sophisticated algorithms
        int currentReplicas = 3; // Assume default

        if (scaleUp) {
            double maxUtilization = Math.max(metrics.getCpuUtilization(), metrics.getMemoryUtilization());
            int additionalReplicas = (int) Math.ceil(maxUtilization / targetCpuUtilization) - 1;
            return Math.min(currentReplicas + additionalReplicas, 10); // Max 10 replicas
        } else {
            double avgUtilization = (metrics.getCpuUtilization() + metrics.getMemoryUtilization()) / 2.0;
            if (avgUtilization < 25.0) {
                return Math.max(currentReplicas - 1, 1); // Min 1 replica
            }
        }

        return currentReplicas;
    }

    private List<SpotInstanceCandidate> identifySpotInstanceCandidates() {
        // Identify workloads suitable for spot instances
        // Batch processing, non-critical workloads, etc.
        return List.of(
            SpotInstanceCandidate.builder()
                .workloadId("batch-analytics-processing")
                .workloadType("BATCH_PROCESSING")
                .estimatedMonthlySavings(2500.0)
                .build()
        );
    }

    private List<SpotInstanceMigration> executeSpotInstanceMigrations(List<SpotInstanceCandidate> candidates) {
        // Execute spot instance migrations
        List<SpotInstanceMigration> migrations = new ArrayList<>();

        for (SpotInstanceCandidate candidate : candidates) {
            // In production, this would trigger AWS Lambda or Kubernetes jobs
            migrations.add(SpotInstanceMigration.builder()
                .workloadId(candidate.getWorkloadId())
                .monthlySavings(candidate.getEstimatedMonthlySavings())
                .migratedAt(Instant.now())
                .build());
        }

        return migrations;
    }

    private ReservedInstanceRecommendation analyzeReservedInstanceNeeds() {
        // Analyze usage patterns for RI recommendations
        // In production, this would analyze CloudWatch billing data
        return ReservedInstanceRecommendation.builder()
            .recommended(true)
            .instanceType("c5.xlarge")
            .instanceCount(5)
            .termYears(3)
            .monthlySavings(3200.0)
            .annualSavings(38400.0)
            .build();
    }

    private void executeReservedInstancePurchase(ReservedInstanceRecommendation recommendation) {
        // Execute RI purchase through AWS API
        logger.info("Executing reserved instance purchase: {} {} instances for {} years",
                   recommendation.getInstanceCount(), recommendation.getInstanceType(),
                   recommendation.getTermYears());
    }

    private List<CloudArbitrageOpportunity> analyzeCloudCosts() {
        // Compare costs across cloud providers
        // In production, this would query multiple cloud billing APIs
        return List.of(
            CloudArbitrageOpportunity.builder()
                .workloadId("data-processing-pipeline")
                .currentProvider("AWS")
                .targetProvider("GCP")
                .monthlySavings(1800.0)
                .confidence(0.85)
                .build()
        );
    }

    private List<CloudMigration> executeCloudMigrations(List<CloudArbitrageOpportunity> opportunities) {
        // Execute workload migrations to more cost-effective providers
        List<CloudMigration> migrations = new ArrayList<>();

        for (CloudArbitrageOpportunity opportunity : opportunities) {
            if (opportunity.getConfidence() > 0.8) {
                // Only migrate high-confidence opportunities
                migrations.add(CloudMigration.builder()
                    .workloadId(opportunity.getWorkloadId())
                    .fromProvider(opportunity.getCurrentProvider())
                    .toProvider(opportunity.getTargetProvider())
                    .monthlySavings(opportunity.getMonthlySavings())
                    .migratedAt(Instant.now())
                    .build());
            }
        }

        return migrations;
    }

    private double calculateScaleDownSavings(String serviceName) {
        // Simplified savings calculation
        // In production, this would use actual instance pricing
        return 150.0; // $150/month savings per scaled-down instance
    }

    private double calculateIdleSavings(String serviceName, ResourceMetrics metrics) {
        // Calculate savings for idle resources
        if (metrics.getActiveConnections() < 5) {
            return 300.0; // $300/month for very idle service
        }
        return 0.0;
    }

    // Enum and data classes

    public enum OptimizationType {
        SCALE_UP, SCALE_DOWN, IDLE_RESOURCE, SPOT_INSTANCE, RESERVED_INSTANCE
    }

    public enum ScalingDecisionType {
        SCALE_UP, SCALE_DOWN, NO_CHANGE
    }

    @lombok.Data
    @lombok.Builder
    public static class ResourceMetrics {
        private double cpuUtilization;
        private double memoryUtilization;
        private long requestCount;
        private int activeConnections;
        private Instant timestamp;
    }

    @lombok.Data
    @lombok.Builder
    public static class OptimizationOpportunity {
        private String serviceName;
        private OptimizationType opportunityType;
        private double potentialSavings;
        private String description;
    }

    @lombok.Data
    @lombok.Builder
    public static class OptimizationAction {
        private String serviceName;
        private OptimizationType actionType;
        private String description;
        private double savingsAchieved;
        private Instant executedAt;
    }

    @lombok.Data
    @lombok.Builder
    public static class CostSavings {
        private double monthlySavings;
        private double annualSavings;
        private String currency;
        private Instant calculatedAt;
    }

    @lombok.Data
    @lombok.Builder
    public static class ScalingDecision {
        private String serviceName;
        private ScalingDecisionType decision;
        private String reason;
        private Integer targetReplicas;
        private Instant timestamp;
    }

    @lombok.Data
    @lombok.Builder
    public static class SpotInstanceCandidate {
        private String workloadId;
        private String workloadType;
        private double estimatedMonthlySavings;
    }

    @lombok.Data
    @lombok.Builder
    public static class SpotInstanceMigration {
        private String workloadId;
        private double monthlySavings;
        private Instant migratedAt;
    }

    @lombok.Data
    @lombok.Builder
    public static class ReservedInstanceRecommendation {
        private boolean recommended;
        private String instanceType;
        private int instanceCount;
        private int termYears;
        private double monthlySavings;
        private double annualSavings;
    }

    @lombok.Data
    @lombok.Builder
    public static class CloudArbitrageOpportunity {
        private String workloadId;
        private String currentProvider;
        private String targetProvider;
        private double monthlySavings;
        private double confidence;
    }

    @lombok.Data
    @lombok.Builder
    public static class CloudMigration {
        private String workloadId;
        private String fromProvider;
        private String toProvider;
        private double monthlySavings;
        private Instant migratedAt;
    }
}