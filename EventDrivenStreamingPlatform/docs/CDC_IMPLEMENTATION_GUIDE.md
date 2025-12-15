# Change Data Capture (CDC) Implementation Guide

## Overview

Change Data Capture (CDC) captures database changes and publishes them as events. The platform implements multiple CDC strategies for different use cases.

---

## CDC Strategies Implemented

### 1. Database Triggers (PostgreSQL) ✅

**Implementation:** `V3__create_cdc_triggers.sql`

**How It Works:**
- PostgreSQL triggers capture INSERT, UPDATE, DELETE operations
- Changes written to `database_change_log` table
- CDC service polls change log and publishes events

**Advantages:**
- No application code changes required
- Captures all changes automatically
- Transactional consistency

**Disadvantages:**
- Database overhead
- Requires trigger maintenance
- Limited filtering capabilities

**Usage:**
```sql
-- Trigger is automatically created for tables
-- Changes are captured automatically
```

### 2. Outbox Pattern ✅

**Implementation:** `OutboxPatternCDC.java`, `PlaybackOutboxService.java`

**How It Works:**
- Application writes events to outbox table in same transaction
- CDC service polls outbox and publishes to Kafka
- Ensures exactly-once semantics

**Advantages:**
- Transactional consistency
- Exactly-once delivery
- Application-controlled events
- No database triggers needed

**Disadvantages:**
- Requires application code changes
- Manual event creation

**Usage:**
```java
@Transactional
public void createPlaybackSession(PlaybackSession session) {
    // Save to database
    sessionRepository.save(session);
    
    // Store event in outbox (same transaction)
    outboxService.storeInOutbox(new PlaybackStartedEvent(...));
}
```

### 3. Debezium Integration ✅

**Implementation:** `DebeziumCDCIntegration.java`

**How It Works:**
- Debezium connector captures database changes
- Publishes change events to Kafka
- This service consumes Debezium events and converts to domain events

**Advantages:**
- Real-time change capture
- No application code changes
- Supports multiple databases
- Transactional consistency

**Disadvantages:**
- Requires Debezium infrastructure
- Additional complexity
- Schema evolution considerations

**Usage:**
```yaml
# Debezium connector configuration (external)
# This service consumes Debezium events from Kafka
```

### 4. Change Data Stream Service ✅

**Implementation:** `ChangeDataStreamService.java`

**How It Works:**
- Provides streaming interface for database changes
- Supports filtering and transformation
- Multiple subscribers support

**Advantages:**
- Flexible filtering
- Multiple subscribers
- Transformation support

**Usage:**
```java
// Stream changes for a specific table
cdcStreamService.streamTableChanges("playback_sessions", 
    ChangeFilters.byOperation("INSERT"),
    event -> {
        // Handle change event
    });
```

---

## Change Log Table Schema

```sql
CREATE TABLE database_change_log (
    id BIGSERIAL PRIMARY KEY,
    table_name VARCHAR(255) NOT NULL,
    operation VARCHAR(20) NOT NULL, -- INSERT, UPDATE, DELETE
    primary_key VARCHAR(255) NOT NULL,
    before_state JSONB,  -- State before change
    after_state JSONB,   -- State after change
    changed_columns TEXT[],  -- Array of changed columns
    transaction_id VARCHAR(255) NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL,
    processed_at TIMESTAMPTZ,  -- When CDC processed it
    failed_at TIMESTAMPTZ,
    error_message TEXT
);
```

---

## CDC Processing Flow

### Trigger-Based CDC

```
Database Change (INSERT/UPDATE/DELETE)
    ↓
Trigger Fires
    ↓
Write to database_change_log
    ↓
CDC Service Polls (every 5 seconds)
    ↓
Convert to Domain Event
    ↓
Publish to Kafka
    ↓
Mark as Processed
```

### Outbox Pattern CDC

```
Application Transaction
    ↓
Save Entity + Store Event in Outbox (same transaction)
    ↓
Transaction Commits
    ↓
CDC Service Polls Outbox (every 1 second)
    ↓
Publish Event to Kafka
    ↓
Mark as Sent
```

### Debezium CDC

```
Database Change
    ↓
Debezium Connector Captures
    ↓
Publish to Kafka (Debezium format)
    ↓
DebeziumCDCIntegration Consumes
    ↓
Convert to Domain Event
    ↓
Publish to Domain Event Topic
```

---

## Configuration

### Application Configuration

```yaml
app:
  cdc:
    enabled: true
    poll-interval-ms: 5000  # Poll every 5 seconds
    batch-size: 100
    use-triggers: true      # Use database triggers
    use-outbox: true         # Use outbox pattern
    use-debezium: false      # Use Debezium (if configured)
    debezium-topic: "debezium.public.playback_sessions"
```

### Database Trigger Configuration

Triggers are created automatically via Flyway migrations:
- `V3__create_cdc_triggers.sql` - Creates triggers for key tables

**Tables with Triggers:**
- `playback_sessions`
- `analytics_events`
- `ml_pipeline_runs`

**Add More Triggers:**
```sql
CREATE TRIGGER cdc_your_table_trigger
    AFTER INSERT OR UPDATE OR DELETE ON your_table
    FOR EACH ROW
    EXECUTE FUNCTION capture_database_change();
```

---

## Best Practices

### 1. Choose the Right CDC Strategy

**Use Database Triggers When:**
- You need to capture all changes automatically
- No application code changes possible
- Legacy system integration

**Use Outbox Pattern When:**
- You want application-controlled events
- You need exactly-once semantics
- You want to avoid database triggers

**Use Debezium When:**
- You need real-time change capture
- You have multiple databases
- You want infrastructure-level CDC

### 2. Performance Considerations

**Polling Interval:**
- Lower interval = lower latency but higher database load
- Recommended: 1-5 seconds depending on requirements

**Batch Size:**
- Larger batches = better throughput but higher memory
- Recommended: 50-200 depending on message size

**Indexing:**
- Index on `processed_at` and `failed_at` for efficient polling
- Index on `table_name` and `operation` for filtering

### 3. Error Handling

**Retry Strategy:**
- Retry transient failures
- Send to DLQ after max retries
- Monitor DLQ for manual intervention

**Failure Tracking:**
- Track failed changes in `database_change_log`
- Alert on high failure rate
- Provide manual reprocessing capability

### 4. Monitoring

**Key Metrics:**
- `cdc.changes.processed` - Processed changes count
- `cdc.changes.failed` - Failed changes count
- `cdc.change.lag` - Time between change and processing

**Alerts:**
- High change lag (> 10 seconds)
- High failure rate (> 1%)
- CDC service down

---

## Usage Examples

### Example 1: Trigger-Based CDC

```java
// No code needed - triggers handle everything
// Changes are automatically captured and published
```

### Example 2: Outbox Pattern CDC

```java
@Service
public class PlaybackService {
    
    @Autowired
    private PlaybackOutboxService outboxService;
    
    @Transactional
    public void startPlayback(String sessionId) {
        // Save entity
        PlaybackSession session = new PlaybackSession(sessionId);
        sessionRepository.save(session);
        
        // Store event in outbox (same transaction)
        BaseEvent event = new PlaybackStartedEvent(sessionId, ...);
        outboxService.storeInOutbox(event);
        
        // Transaction commits - event will be published by CDC
    }
}
```

### Example 3: Change Data Stream

```java
@Service
public class ChangeStreamSubscriber {
    
    @Autowired
    private ChangeDataStreamService cdcStreamService;
    
    @PostConstruct
    public void subscribe() {
        // Subscribe to INSERT operations on playback_sessions
        cdcStreamService.streamTableChanges("playback_sessions",
            ChangeFilters.byOperation("INSERT"),
            event -> {
                // Handle new playback session
                handleNewPlaybackSession(event);
            });
    }
}
```

### Example 4: Filtered Changes

```java
// Stream only UPDATE operations that change 'status' column
ChangeFilter filter = ChangeFilters.and(
    ChangeFilters.byOperation("UPDATE"),
    ChangeFilters.byColumnChange("status")
);

cdcStreamService.streamFilteredChanges(filter, event -> {
    // Handle status changes
});
```

---

## Troubleshooting

### High Change Lag

**Symptoms:**
- Changes not processed quickly
- High `cdc.change.lag` metric

**Solutions:**
1. Reduce polling interval
2. Increase batch size
3. Scale CDC service instances
4. Optimize change log queries

### High Failure Rate

**Symptoms:**
- Many changes in `failed_at` state
- High `cdc.changes.failed` metric

**Solutions:**
1. Check error messages in change log
2. Review event publishing logic
3. Check Kafka connectivity
4. Review event schema

### Missing Changes

**Symptoms:**
- Some changes not captured

**Solutions:**
1. Verify triggers are created
2. Check trigger function is correct
3. Verify outbox pattern is used correctly
4. Check Debezium connector status

---

## Production Checklist

- [ ] Change log table created
- [ ] Database triggers created for key tables
- [ ] CDC service configured and running
- [ ] Polling interval tuned
- [ ] Batch size optimized
- [ ] Error handling configured
- [ ] DLQ configured for failed changes
- [ ] Metrics collection enabled
- [ ] Alerts configured
- [ ] Monitoring dashboards created
- [ ] Load testing completed

---

*Last Updated: 2024*

