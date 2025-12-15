package com.netflix.streaming.infrastructure.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Event deduplication service for ensuring exactly-once event processing.
 * 
 * Uses both Redis (for fast lookups) and database (for persistence) to track
 * processed events and prevent duplicate processing.
 */
@Service
public class EventDeduplicationService {

    private static final Logger logger = LoggerFactory.getLogger(EventDeduplicationService.class);
    private static final String REDIS_KEY_PREFIX = "event:dedup:";
    private static final Duration REDIS_TTL = Duration.ofDays(7); // 7 days in Redis
    private static final int DB_RETENTION_DAYS = 7; // 7 days in database

    private final StringRedisTemplate redisTemplate;
    private final JdbcTemplate jdbcTemplate;

    public EventDeduplicationService(StringRedisTemplate redisTemplate, JdbcTemplate jdbcTemplate) {
        this.redisTemplate = redisTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Check if an event has already been processed.
     * 
     * @param eventId The event ID
     * @param consumerName The name of the consumer processing the event
     * @return true if event was already processed, false otherwise
     */
    public boolean isDuplicate(String eventId, String consumerName) {
        if (eventId == null || consumerName == null) {
            return false;
        }

        // First check Redis (fast path)
        String redisKey = buildRedisKey(eventId, consumerName);
        Boolean existsInRedis = redisTemplate.hasKey(redisKey);
        if (Boolean.TRUE.equals(existsInRedis)) {
            logger.debug("Event {} already processed by {} (found in Redis)", eventId, consumerName);
            return true;
        }

        // Fallback to database check (slower but persistent)
        boolean existsInDb = checkDatabase(eventId, consumerName);
        if (existsInDb) {
            // Cache in Redis for future lookups
            redisTemplate.opsForValue().set(redisKey, "1", REDIS_TTL);
            logger.debug("Event {} already processed by {} (found in database)", eventId, consumerName);
            return true;
        }

        return false;
    }

    /**
     * Record that an event has been processed.
     * 
     * @param eventId The event ID
     * @param consumerName The name of the consumer that processed the event
     */
    public void recordProcessed(String eventId, String consumerName) {
        if (eventId == null || consumerName == null) {
            return;
        }

        try {
            // Store in Redis (fast, with TTL)
            String redisKey = buildRedisKey(eventId, consumerName);
            redisTemplate.opsForValue().set(redisKey, "1", REDIS_TTL);

            // Store in database (persistent)
            storeInDatabase(eventId, consumerName);

            logger.debug("Recorded event {} as processed by {}", eventId, consumerName);

        } catch (Exception e) {
            logger.error("Failed to record processed event: {} by consumer: {}", eventId, consumerName, e);
            // Don't throw - deduplication failure shouldn't break event processing
        }
    }

    /**
     * Record batch of processed events.
     */
    public void recordProcessedBatch(java.util.List<String> eventIds, String consumerName) {
        if (eventIds == null || eventIds.isEmpty() || consumerName == null) {
            return;
        }

        for (String eventId : eventIds) {
            recordProcessed(eventId, consumerName);
        }
    }

    /**
     * Clean up old deduplication records from database.
     * Should be called periodically (e.g., daily cron job).
     */
    public void cleanupOldRecords() {
        try {
            String sql = """
                DELETE FROM event_deduplication
                WHERE processed_at < NOW() - INTERVAL '%d days'
                """.formatted(DB_RETENTION_DAYS);

            int deleted = jdbcTemplate.update(sql);
            if (deleted > 0) {
                logger.info("Cleaned up {} old deduplication records", deleted);
            }
        } catch (Exception e) {
            logger.error("Failed to cleanup old deduplication records", e);
        }
    }

    /**
     * Get deduplication statistics.
     */
    public DeduplicationStats getStats() {
        try {
            String sql = """
                SELECT 
                    COUNT(*) as total,
                    COUNT(CASE WHEN processed_at > NOW() - INTERVAL '1 hour' THEN 1 END) as last_hour,
                    COUNT(CASE WHEN processed_at > NOW() - INTERVAL '24 hours' THEN 1 END) as last_24_hours
                FROM event_deduplication
                """;

            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> 
                new DeduplicationStats(
                    rs.getLong("total"),
                    rs.getLong("last_hour"),
                    rs.getLong("last_24_hours")
                )
            );
        } catch (Exception e) {
            logger.error("Failed to get deduplication stats", e);
            return new DeduplicationStats(0, 0, 0);
        }
    }

    /**
     * Check if event exists in database.
     */
    private boolean checkDatabase(String eventId, String consumerName) {
        try {
            String sql = """
                SELECT COUNT(*) 
                FROM event_deduplication 
                WHERE event_id = ? AND consumer_name = ?
                """;

            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, eventId, consumerName);
            return count != null && count > 0;
        } catch (Exception e) {
            logger.debug("Event not found in database: {} by {}", eventId, consumerName);
            return false;
        }
    }

    /**
     * Store event in database.
     */
    private void storeInDatabase(String eventId, String consumerName) {
        try {
            String sql = """
                INSERT INTO event_deduplication (event_id, consumer_name, processed_at)
                VALUES (?, ?, NOW())
                ON CONFLICT (event_id, consumer_name) DO NOTHING
                """;

            jdbcTemplate.update(sql, eventId, consumerName);
        } catch (Exception e) {
            logger.error("Failed to store deduplication record in database", e);
            // Don't throw - Redis storage succeeded, that's sufficient
        }
    }

    /**
     * Build Redis key for event deduplication.
     */
    private String buildRedisKey(String eventId, String consumerName) {
        return REDIS_KEY_PREFIX + consumerName + ":" + eventId;
    }

    /**
     * Deduplication statistics.
     */
    public static class DeduplicationStats {
        private final long total;
        private final long lastHour;
        private final long last24Hours;

        public DeduplicationStats(long total, long lastHour, long last24Hours) {
            this.total = total;
            this.lastHour = lastHour;
            this.last24Hours = last24Hours;
        }

        public long getTotal() { return total; }
        public long getLastHour() { return lastHour; }
        public long getLast24Hours() { return last24Hours; }
    }
}

