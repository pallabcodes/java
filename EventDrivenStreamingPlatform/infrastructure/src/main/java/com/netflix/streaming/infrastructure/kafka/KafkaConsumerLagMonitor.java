package com.netflix.streaming.infrastructure.kafka;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Kafka Consumer Lag Monitor.
 * 
 * Monitors consumer lag for all consumer groups and exposes metrics.
 * Provides alerting capabilities for high lag scenarios.
 */
@Service
public class KafkaConsumerLagMonitor {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerLagMonitor.class);

    private final AdminClient adminClient;
    private final MeterRegistry meterRegistry;
    private final Map<String, Long> consumerLagCache = new HashMap<>();

    public KafkaConsumerLagMonitor(KafkaAdmin kafkaAdmin, MeterRegistry meterRegistry) {
        this.adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties());
        this.meterRegistry = meterRegistry;
        
        // Register lag metrics
        Gauge.builder("kafka.consumer.lag", consumerLagCache, cache -> {
            return cache.values().stream().mapToLong(Long::longValue).sum();
        })
        .description("Total consumer lag across all consumer groups")
        .register(meterRegistry);
    }

    /**
     * Monitor consumer lag for a specific consumer group.
     */
    public ConsumerLagInfo getConsumerLag(String consumerGroup) {
        try {
            // Get consumer group offsets
            ListConsumerGroupOffsetsResult offsetsResult = adminClient.listConsumerGroupOffsets(consumerGroup);
            Map<TopicPartition, OffsetAndMetadata> committedOffsets = offsetsResult.partitionsToOffsetAndMetadata().get();

            // Get topic end offsets
            Set<TopicPartition> partitions = committedOffsets.keySet();
            Map<TopicPartition, Long> endOffsets = adminClient.listOffsets(
                partitions.stream().collect(Collectors.toMap(
                    tp -> tp,
                    tp -> org.apache.kafka.clients.admin.ListOffsetsOptions.latest()
                ))
            ).all().get().entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().offset()
                ));

            // Calculate lag
            Map<TopicPartition, Long> lagMap = new HashMap<>();
            long totalLag = 0;

            for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : committedOffsets.entrySet()) {
                TopicPartition partition = entry.getKey();
                long committedOffset = entry.getValue().offset();
                long endOffset = endOffsets.getOrDefault(partition, 0L);
                long lag = Math.max(0, endOffset - committedOffset);
                
                lagMap.put(partition, lag);
                totalLag += lag;

                // Update metrics
                String metricKey = consumerGroup + ":" + partition.topic() + ":" + partition.partition();
                consumerLagCache.put(metricKey, lag);
                
                meterRegistry.gauge("kafka.consumer.lag.by.partition", 
                    Arrays.asList(
                        "consumer.group", consumerGroup,
                        "topic", partition.topic(),
                        "partition", String.valueOf(partition.partition())
                    ),
                    lag);
            }

            return new ConsumerLagInfo(consumerGroup, lagMap, totalLag);

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Failed to get consumer lag for group: {}", consumerGroup, e);
            return new ConsumerLagInfo(consumerGroup, Collections.emptyMap(), 0L);
        }
    }

    /**
     * Get lag for all consumer groups.
     */
    public Map<String, ConsumerLagInfo> getAllConsumerLag() {
        try {
            Map<String, ConsumerGroupDescription> groups = adminClient.describeConsumerGroups(
                adminClient.listConsumerGroups().all().get().stream()
                    .map(g -> g.groupId())
                    .collect(Collectors.toList())
            ).all().get();

            Map<String, ConsumerLagInfo> lagInfo = new HashMap<>();
            for (String groupId : groups.keySet()) {
                lagInfo.put(groupId, getConsumerLag(groupId));
            }

            return lagInfo;

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Failed to get consumer lag for all groups", e);
            return Collections.emptyMap();
        }
    }

    /**
     * Check if consumer lag exceeds threshold.
     */
    public boolean isLagHigh(String consumerGroup, long threshold) {
        ConsumerLagInfo lagInfo = getConsumerLag(consumerGroup);
        return lagInfo.getTotalLag() > threshold;
    }

    /**
     * Scheduled task to monitor consumer lag (runs every 30 seconds).
     */
    @Scheduled(fixedRate = 30000)
    public void monitorConsumerLag() {
        try {
            Map<String, ConsumerLagInfo> allLag = getAllConsumerLag();
            
            for (ConsumerLagInfo lagInfo : allLag.values()) {
                if (lagInfo.getTotalLag() > 10000) {
                    logger.warn("High consumer lag detected: group={}, lag={}",
                        lagInfo.getConsumerGroup(), lagInfo.getTotalLag());
                }
            }

        } catch (Exception e) {
            logger.error("Error monitoring consumer lag", e);
        }
    }

    /**
     * Consumer lag information.
     */
    public static class ConsumerLagInfo {
        private final String consumerGroup;
        private final Map<TopicPartition, Long> lagByPartition;
        private final long totalLag;

        public ConsumerLagInfo(String consumerGroup, Map<TopicPartition, Long> lagByPartition, long totalLag) {
            this.consumerGroup = consumerGroup;
            this.lagByPartition = lagByPartition;
            this.totalLag = totalLag;
        }

        public String getConsumerGroup() { return consumerGroup; }
        public Map<TopicPartition, Long> getLagByPartition() { return lagByPartition; }
        public long getTotalLag() { return totalLag; }
    }
}

