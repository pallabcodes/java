# Performance Tuning Guide
## Event-Driven Streaming Platform

## Overview

This guide provides comprehensive performance tuning recommendations for production deployments. All configurations should be tested under load before applying to production.

---

## Database Performance Tuning

### Connection Pool Configuration

**Production Recommendations:**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50  # Adjust based on DB server capacity
      minimum-idle: 10       # Keep warm connections ready
      connection-timeout: 30000
      idle-timeout: 600000   # 10 minutes
      max-lifetime: 1800000  # 30 minutes
      leak-detection-threshold: 60000
```

**Formula for Pool Size:**
```
connections = ((core_count * 2) + effective_spindle_count)
```

### Query Optimization

1. **Batch Processing:**
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 25
        order_inserts: true
        order_updates: true
        batch_versioned_data: true
```

2. **Index Optimization:**
- Ensure indexes on frequently queried columns
- Composite indexes for multi-column queries
- Partial indexes for filtered queries

3. **Connection Pool Monitoring:**
```sql
-- Monitor active connections
SELECT count(*) FROM pg_stat_activity WHERE state = 'active';

-- Monitor connection pool usage
SELECT 
    max_conn,
    used_conn,
    (max_conn - used_conn) as available_conn
FROM (
    SELECT 
        setting::int as max_conn,
        (SELECT count(*) FROM pg_stat_activity) as used_conn
    FROM pg_settings 
    WHERE name = 'max_connections'
) t;
```

---

## Kafka Performance Tuning

### Producer Configuration

**High Throughput:**
```yaml
spring:
  kafka:
    producer:
      acks: 1                    # Faster than 'all'
      retries: 3
      batch-size: 32768          # 32KB batches
      linger-ms: 10              # Wait 10ms for batching
      compression-type: lz4      # Good balance
      buffer-memory: 67108864    # 64MB buffer
```

**High Reliability:**
```yaml
spring:
  kafka:
    producer:
      acks: all                  # Wait for all replicas
      retries: 2147483647        # Retry indefinitely
      max-in-flight-requests-per-connection: 1
      enable-idempotence: true
      compression-type: snappy   # Better compression
```

### Consumer Configuration

**High Throughput:**
```yaml
spring:
  kafka:
    consumer:
      max-poll-records: 500      # Process more per poll
      fetch-min-bytes: 1048576   # 1MB minimum fetch
      fetch-max-wait: 500ms
      session-timeout-ms: 30000
      heartbeat-interval-ms: 10000
```

**Low Latency:**
```yaml
spring:
  kafka:
    consumer:
      max-poll-records: 10       # Smaller batches
      fetch-min-bytes: 1         # Don't wait for more
      fetch-max-wait: 0          # Don't wait
```

### Partition Strategy

- **Number of Partitions:** `max(desired_throughput / single_partition_throughput, number_of_consumers)`
- **Replication Factor:** 3 for production (ensures durability)
- **Partition Key:** Use high-cardinality keys for even distribution

---

## Redis Performance Tuning

### Connection Pool

```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
        shutdown-timeout: 200ms
```

### Memory Optimization

1. **Key Expiration:** Set appropriate TTLs
2. **Memory Policy:** `maxmemory-policy allkeys-lru`
3. **Compression:** Consider for large values

### Monitoring

```bash
# Monitor memory usage
redis-cli INFO memory

# Monitor connections
redis-cli INFO clients

# Monitor commands
redis-cli INFO stats
```

---

## JVM Performance Tuning

### Memory Configuration

**Recommended Settings:**
```bash
-Xms2g -Xmx4g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m
-XX:+UseStringDeduplication
```

**For High Throughput:**
```bash
-Xms4g -Xmx8g
-XX:+UseParallelGC
-XX:ParallelGCThreads=4
```

**For Low Latency:**
```bash
-Xms2g -Xmx4g
-XX:+UseZGC
-XX:+UnlockExperimentalVMOptions
```

### GC Tuning

1. **G1GC (Recommended):**
```bash
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m
-XX:InitiatingHeapOccupancyPercent=45
```

2. **Parallel GC (High Throughput):**
```bash
-XX:+UseParallelGC
-XX:ParallelGCThreads=4
-XX:MaxGCPauseMillis=200
```

### Monitoring

```bash
# GC Logging
-XX:+PrintGCDetails
-XX:+PrintGCDateStamps
-Xloggc:/var/log/gc.log

# Heap Dump on OOM
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/var/log/heap-dump.hprof
```

---

## Application-Level Tuning

### Thread Pool Configuration

**Web Server Threads:**
```yaml
server:
  tomcat:
    threads:
      max: 200
      min-spare: 10
    connection-timeout: 20000
    max-connections: 8192
    accept-count: 100
```

**Async Processing:**
```yaml
spring:
  task:
    execution:
      pool:
        core-size: 8
        max-size: 16
        queue-capacity: 100
        keep-alive: 60s
```

### Caching Strategy

1. **Local Cache (Caffeine):**
```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=1h
```

2. **Distributed Cache (Redis):**
- Use for shared state across instances
- Set appropriate TTLs
- Monitor cache hit rates

### Batch Processing

**Event Processing:**
```java
// Process events in batches
@KafkaListener(topics = "events", batch = true)
public void processBatch(List<ConsumerRecord<String, Event>> records) {
    // Batch processing logic
}
```

---

## Network Performance

### HTTP/2 Configuration

```yaml
server:
  http2:
    enabled: true
```

### Compression

```yaml
server:
  compression:
    enabled: true
    mime-types: application/json,application/xml
    min-response-size: 1024
```

---

## Monitoring and Metrics

### Key Metrics to Monitor

1. **Application Metrics:**
   - Request rate (RPS)
   - Response time (p50, p95, p99)
   - Error rate
   - Active requests

2. **Database Metrics:**
   - Connection pool usage
   - Query execution time
   - Transaction rate
   - Lock wait time

3. **Kafka Metrics:**
   - Consumer lag
   - Producer throughput
   - Partition distribution
   - Replication lag

4. **JVM Metrics:**
   - Heap usage
   - GC frequency and duration
   - Thread count
   - CPU usage

### Performance Targets

- **Response Time:** p95 < 200ms, p99 < 500ms
- **Throughput:** > 1000 RPS per instance
- **Error Rate:** < 0.1%
- **Availability:** 99.9% uptime

---

## Load Testing

### Recommended Tools

1. **JMeter:** For HTTP load testing
2. **Gatling:** For high-performance testing
3. **k6:** For modern load testing
4. **Apache Bench:** For quick tests

### Test Scenarios

1. **Baseline:** Measure current performance
2. **Ramp-up:** Gradually increase load
3. **Sustained:** Maintain peak load
4. **Spike:** Sudden load increase
5. **Stress:** Beyond capacity

### Performance Benchmarks

- **Single Instance:** 1000-2000 RPS
- **Database:** 10,000 queries/sec
- **Kafka:** 50,000 messages/sec
- **Redis:** 100,000 ops/sec

---

## Troubleshooting Performance Issues

### High Response Time

1. Check database query performance
2. Review Kafka consumer lag
3. Monitor connection pool usage
4. Check JVM GC logs
5. Review network latency

### High Memory Usage

1. Check for memory leaks
2. Review cache sizes
3. Optimize batch sizes
4. Check connection pool sizes
5. Review JVM heap settings

### High CPU Usage

1. Profile application code
2. Check for inefficient algorithms
3. Review thread pool sizes
4. Check for CPU-intensive operations
5. Review GC frequency

---

## Production Checklist

- [ ] Database connection pool sized correctly
- [ ] Kafka producer/consumer tuned for workload
- [ ] Redis connection pool configured
- [ ] JVM memory and GC settings optimized
- [ ] Thread pools sized appropriately
- [ ] Caching strategy implemented
- [ ] Compression enabled
- [ ] Monitoring and alerting configured
- [ ] Load testing completed
- [ ] Performance baselines established

---

*Last Updated: 2024*

