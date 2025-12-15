# Kafka Implementation Guide
## Event-Driven Streaming Platform

## Overview

This guide documents the complete Kafka implementation for the Event-Driven Streaming Platform, including production-grade features, error handling, monitoring, and best practices.

---

## Architecture

### Event Bus Design

The platform uses Kafka as the event bus for:
- **Event Publishing**: Domain events published to Kafka topics
- **Event Consumption**: Services consume events from Kafka topics
- **Event Routing**: Topic-based routing for different event types
- **Event Replay**: Ability to replay events from Kafka

### Topic Structure

```
playback.events          - Playback telemetry events
playback.commands        - Playback control commands
analytics.events         - Analytics events
analytics.commands       - Analytics processing commands
ml.events               - ML pipeline events
ml.commands             - ML processing commands
dashboard.events        - Real-time dashboard events
system.events           - System health events
*.dlq                   - Dead letter queues (one per topic)
```

---

## Implementation Components

### 1. Kafka Configuration (`KafkaEventBusConfig`)

**Features:**
- Producer factory with reliability settings
- Consumer factory with manual commit
- Topic auto-creation
- Error handling configuration
- Retry template configuration

**Key Settings:**
```java
// Producer Reliability
acks: all
retries: 3
enable-idempotence: true
max-in-flight-requests-per-connection: 1

// Consumer Reliability
enable-auto-commit: false
auto-offset-reset: earliest
max-poll-records: 100
```

### 2. Event Publisher (`KafkaEventPublisher`)

**Features:**
- Reliable event publishing
- OpenTelemetry tracing integration
- Topic routing based on event type
- Partition key determination
- Error handling

**Usage:**
```java
@Autowired
private EventPublisher eventPublisher;

eventPublisher.publish(new PlaybackStartedEvent(...));
```

### 3. Dead Letter Queue Service (`KafkaDeadLetterQueueService`)

**Features:**
- Automatic DLQ routing for failed messages
- Comprehensive error metadata
- Retry count tracking
- Error classification

**DLQ Message Structure:**
```java
{
  "originalTopic": "playback.events",
  "originalKey": "session-123",
  "originalValue": {...},
  "errorMessage": "Processing failed",
  "errorType": "ProcessingException",
  "stackTrace": "...",
  "retryCount": 3,
  "consumerGroup": "analytics-service",
  "failedAt": "2024-01-01T10:00:00Z"
}
```

### 4. Consumer Lag Monitor (`KafkaConsumerLagMonitor`)

**Features:**
- Real-time consumer lag monitoring
- Per-partition lag tracking
- Metrics exposure
- Scheduled monitoring (every 30 seconds)
- Alerting on high lag

**Usage:**
```java
@Autowired
private KafkaConsumerLagMonitor lagMonitor;

ConsumerLagInfo lagInfo = lagMonitor.getConsumerLag("analytics-service");
long totalLag = lagInfo.getTotalLag();
```

### 5. Error Handler (`KafkaErrorHandler`)

**Features:**
- Retryable vs non-retryable exception classification
- Exponential backoff retry
- Automatic DLQ routing after max retries
- Retry count tracking

**Retryable Exceptions:**
- `KafkaException`
- `SocketTimeoutException`
- `RetriableException`

**Non-Retryable Exceptions:**
- `IllegalArgumentException`
- `RecordTooLargeException`
- `SerializationException`

### 6. Metrics Service (`KafkaMetricsService`)

**Metrics Exposed:**
- `kafka.producer.messages.published` - Published message count
- `kafka.producer.messages.failed` - Failed publication count
- `kafka.consumer.messages.consumed` - Consumed message count
- `kafka.consumer.messages.failed` - Failed consumption count
- `kafka.consumer.processing.time` - Message processing time
- `kafka.consumer.lag` - Consumer lag by partition
- `kafka.dlq.messages` - DLQ message count
- `kafka.producer.batch.size` - Producer batch size
- `kafka.consumer.poll.time` - Consumer poll time

### 7. Batch Processor (`KafkaBatchProcessor`)

**Features:**
- Batch message processing
- Transactional batch commits
- Batch error handling
- Batch metrics

**Usage:**
```java
@KafkaListener(topics = "playback.events", batch = true)
public void processBatch(List<ConsumerRecord<String, BaseEvent>> records) {
    // Process batch
}
```

---

## Error Handling Strategy

### Retry Strategy

1. **Transient Failures**: Retry with exponential backoff
   - Initial delay: 1 second
   - Multiplier: 2.0
   - Max delay: 10 seconds
   - Max retries: 3

2. **Permanent Failures**: Send directly to DLQ
   - No retries for non-retryable exceptions
   - Immediate DLQ routing

3. **Max Retries Exceeded**: Send to DLQ
   - After 3 retry attempts
   - With full error metadata

### DLQ Processing

1. **DLQ Message Format**: Includes original message + error metadata
2. **DLQ Monitoring**: Metrics and alerts for DLQ messages
3. **DLQ Reprocessing**: Manual reprocessing capability (future)

---

## Monitoring and Observability

### Metrics

**Producer Metrics:**
- Message publish rate
- Publish failure rate
- Batch size
- Latency

**Consumer Metrics:**
- Message consumption rate
- Processing time (p50, p95, p99)
- Consumer lag
- Failure rate

**DLQ Metrics:**
- DLQ message count
- DLQ message rate
- Error types in DLQ

### Alerts

**Prometheus Alerts:**
- High consumer lag (> 10,000 messages)
- High error rate (> 1%)
- DLQ message rate (> 10 messages/min)
- Consumer group down

### Dashboards

**Grafana Dashboards:**
- Kafka consumer lag dashboard
- Kafka producer/consumer throughput
- Error rate dashboard
- DLQ dashboard

---

## Best Practices

### Producer Best Practices

1. **Use Idempotent Producer**
   ```java
   enable-idempotence: true
   max-in-flight-requests-per-connection: 1
   ```

2. **Use Appropriate Partition Keys**
   - Use aggregate ID for ordering
   - Use correlation ID for related events

3. **Batch Messages**
   ```java
   batch-size: 16384
   linger-ms: 10
   ```

4. **Handle Failures**
   - Use callbacks for async publishing
   - Log failures with context
   - Retry transient failures

### Consumer Best Practices

1. **Manual Commit**
   ```java
   enable-auto-commit: false
   ack-mode: MANUAL_IMMEDIATE
   ```

2. **Handle Errors Gracefully**
   - Classify retryable vs non-retryable
   - Use exponential backoff
   - Send to DLQ after max retries

3. **Monitor Consumer Lag**
   - Set up lag monitoring
   - Alert on high lag
   - Scale consumers if needed

4. **Use Batch Processing**
   - Process messages in batches
   - Commit entire batch
   - Handle batch errors

### Topic Design Best Practices

1. **Topic Naming**
   - Use domain.event or domain.command pattern
   - Use lowercase with dots
   - Be descriptive

2. **Partitioning**
   - Number of partitions = max(desired_throughput / single_partition_throughput, number_of_consumers)
   - Use partition keys for ordering

3. **Retention**
   - Set appropriate retention (7 days default)
   - Consider compaction for key-based topics

4. **Replication**
   - Use replication factor of 3 for production
   - Ensures durability

---

## Performance Tuning

### Producer Tuning

**High Throughput:**
```yaml
acks: 1
compression-type: lz4
batch-size: 32768
linger-ms: 10
```

**High Reliability:**
```yaml
acks: all
enable-idempotence: true
max-in-flight-requests-per-connection: 1
```

### Consumer Tuning

**High Throughput:**
```yaml
max-poll-records: 500
fetch-min-bytes: 1048576
fetch-max-wait: 500ms
```

**Low Latency:**
```yaml
max-poll-records: 10
fetch-min-bytes: 1
fetch-max-wait: 0
```

---

## Troubleshooting

### High Consumer Lag

**Symptoms:**
- Consumer lag increasing
- Processing delays

**Solutions:**
1. Scale consumers (increase consumer instances)
2. Increase consumer parallelism
3. Optimize processing logic
4. Check for blocking operations

### High Error Rate

**Symptoms:**
- Many messages in DLQ
- High failure rate

**Solutions:**
1. Check error logs
2. Review error types
3. Fix processing logic
4. Adjust retry strategy

### Producer Failures

**Symptoms:**
- Messages not published
- Producer errors

**Solutions:**
1. Check Kafka connectivity
2. Review producer configuration
3. Check topic exists
4. Review error logs

---

## Production Checklist

- [ ] Topics created with appropriate partitions and replication
- [ ] Producer configured with idempotence
- [ ] Consumer configured with manual commit
- [ ] Error handling configured with DLQ
- [ ] Consumer lag monitoring enabled
- [ ] Metrics collection configured
- [ ] Alerts configured
- [ ] Dashboards created
- [ ] Retry strategy configured
- [ ] DLQ topics created
- [ ] Performance tuning applied
- [ ] Load testing completed

---

*Last Updated: 2024*

