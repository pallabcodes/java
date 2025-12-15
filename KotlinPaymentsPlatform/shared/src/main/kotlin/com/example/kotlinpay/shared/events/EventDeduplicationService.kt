package com.example.kotlinpay.shared.events

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.time.Duration

/**
 * Event deduplication service for ensuring exactly-once event processing.
 */
@Service
class EventDeduplicationService(
    private val redisTemplate: StringRedisTemplate,
    private val jdbcTemplate: JdbcTemplate
) {
    private val logger = LoggerFactory.getLogger(EventDeduplicationService::class.java)
    
    companion object {
        private const val REDIS_KEY_PREFIX = "event:dedup:"
        private val REDIS_TTL = Duration.ofDays(7)
        private const val DB_RETENTION_DAYS = 7
    }

    /**
     * Check if an event has already been processed.
     */
    fun isDuplicate(eventId: String?, consumerName: String?): Boolean {
        if (eventId == null || consumerName == null) {
            return false
        }

        val redisKey = buildRedisKey(eventId, consumerName)
        if (redisTemplate.hasKey(redisKey) == true) {
            logger.debug("Event {} already processed by {} (found in Redis)", eventId, consumerName)
            return true
        }

        val existsInDb = checkDatabase(eventId, consumerName)
        if (existsInDb) {
            redisTemplate.opsForValue().set(redisKey, "1", REDIS_TTL)
            logger.debug("Event {} already processed by {} (found in database)", eventId, consumerName)
            return true
        }

        return false
    }

    /**
     * Record that an event has been processed.
     */
    fun recordProcessed(eventId: String?, consumerName: String?) {
        if (eventId == null || consumerName == null) {
            return
        }

        try {
            val redisKey = buildRedisKey(eventId, consumerName)
            redisTemplate.opsForValue().set(redisKey, "1", REDIS_TTL)
            storeInDatabase(eventId, consumerName)
            logger.debug("Recorded event {} as processed by {}", eventId, consumerName)
        } catch (e: Exception) {
            logger.error("Failed to record processed event: {} by consumer: {}", eventId, consumerName, e)
        }
    }

    /**
     * Clean up old deduplication records.
     */
    fun cleanupOldRecords() {
        try {
            val sql = """
                DELETE FROM event_deduplication
                WHERE processed_at < NOW() - INTERVAL '$DB_RETENTION_DAYS days'
            """.trimIndent()

            val deleted = jdbcTemplate.update(sql)
            if (deleted > 0) {
                logger.info("Cleaned up {} old deduplication records", deleted)
            }
        } catch (e: Exception) {
            logger.error("Failed to cleanup old deduplication records", e)
        }
    }

    private fun checkDatabase(eventId: String, consumerName: String): Boolean {
        return try {
            val sql = """
                SELECT COUNT(*) 
                FROM event_deduplication 
                WHERE event_id = ? AND consumer_name = ?
            """.trimIndent()

            val count = jdbcTemplate.queryForObject(sql, Int::class.java, eventId, consumerName)
            count != null && count > 0
        } catch (e: Exception) {
            logger.debug("Event not found in database: {} by {}", eventId, consumerName)
            false
        }
    }

    private fun storeInDatabase(eventId: String, consumerName: String) {
        try {
            val sql = """
                INSERT INTO event_deduplication (event_id, consumer_name, processed_at)
                VALUES (?, ?, NOW())
                ON CONFLICT (event_id, consumer_name) DO NOTHING
            """.trimIndent()

            jdbcTemplate.update(sql, eventId, consumerName)
        } catch (e: Exception) {
            logger.error("Failed to store deduplication record in database", e)
        }
    }

    private fun buildRedisKey(eventId: String, consumerName: String): String {
        return "$REDIS_KEY_PREFIX$consumerName:$eventId"
    }
}

