package com.netflix.streaming.infrastructure.cdc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.streaming.events.BaseEvent;
import com.netflix.streaming.events.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeDataCaptureServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private ChangeDataCaptureMetrics metrics;

    private ObjectMapper objectMapper;
    private ChangeDataCaptureService cdcService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        cdcService = new ChangeDataCaptureService(jdbcTemplate, eventPublisher, objectMapper, metrics);
    }

    @Test
    void shouldProcessPendingChangeLogEntries() {
        // Given
        ChangeDataCaptureService.ChangeLogEntry entry = createTestChangeLogEntry();
        when(jdbcTemplate.query(any(String.class), any(), any(Class.class))).thenReturn(List.of(entry));
        doNothing().when(eventPublisher).publish(any(BaseEvent.class));

        // When
        cdcService.processChangeLog();

        // Then
        verify(jdbcTemplate).query(any(String.class), any(), any(Class.class));
        verify(eventPublisher).publish(any(BaseEvent.class));
        verify(metrics).recordChangeProcessed(eq("playback_sessions"), eq("INSERT"));
        verify(jdbcTemplate).update(eq("UPDATE database_change_log SET processed_at = NOW() WHERE id = ?"), eq(1L));
    }

    @Test
    void shouldTriggerCdcForSpecificTable() {
        // Given
        String tableName = "playback_sessions";
        String primaryKeyColumn = "id";
        Object primaryKeyValue = "session-123";

        Map<String, Object> rowData = Map.of(
            "id", "session-123",
            "user_id", "user-456",
            "content_id", "movie-789",
            "status", "PLAYING"
        );

        when(jdbcTemplate.queryForMap(any(String.class), eq(primaryKeyValue))).thenReturn(rowData);
        doNothing().when(eventPublisher).publish(any(BaseEvent.class));

        // When
        cdcService.triggerCdcForTable(tableName, primaryKeyColumn, primaryKeyValue);

        // Then
        verify(jdbcTemplate).queryForMap(any(String.class), eq(primaryKeyValue));
        verify(eventPublisher).publish(any(BaseEvent.class));
        verify(metrics).recordChangeProcessed(eq(tableName), eq("MANUAL"));
    }

    @Test
    void shouldHandleFailedChangeProcessing() {
        // Given
        ChangeDataCaptureService.ChangeLogEntry entry = createTestChangeLogEntry();
        when(jdbcTemplate.query(any(String.class), any(), any(Class.class))).thenReturn(List.of(entry));
        doThrow(new RuntimeException("Processing failed")).when(eventPublisher).publish(any(BaseEvent.class));

        // When
        cdcService.processChangeLog();

        // Then
        verify(jdbcTemplate).query(any(String.class), any(), any(Class.class));
        verify(eventPublisher).publish(any(BaseEvent.class));
        verify(metrics).recordChangeFailed(eq("playback_sessions"), eq("INSERT"));
        verify(jdbcTemplate).update(eq("UPDATE database_change_log SET failed_at = NOW(), error_message = ? WHERE id = ?"),
                                   eq("Processing failed"), eq(1L));
    }

    @Test
    void shouldParseJsonCorrectly() {
        // Given
        String json = "{\"id\": \"123\", \"name\": \"test\"}";

        // When
        Map<String, Object> result = cdcService.parseJson(json);

        // Then
        assert result != null;
        assert result.get("id").equals("123");
        assert result.get("name").equals("test");
    }

    @Test
    void shouldHandleInvalidJsonGracefully() {
        // Given
        String invalidJson = "invalid json";

        // When
        Map<String, Object> result = cdcService.parseJson(invalidJson);

        // Then
        assert result != null;
        assert result.isEmpty();
    }

    @Test
    void shouldConvertSnakeCaseToCamelCase() {
        // When
        String result = cdcService.toCamelCase("playback_sessions");

        // Then
        assert result.equals("PlaybackSessions");
    }

    @Test
    void shouldCapitalizeFirstLetter() {
        // When
        String result = cdcService.capitalize("insert");

        // Then
        assert result.equals("Insert");
    }

    private ChangeDataCaptureService.ChangeLogEntry createTestChangeLogEntry() {
        return new ChangeDataCaptureService.ChangeLogEntry(
            1L,
            "playback_sessions",
            "INSERT",
            "session-123",
            null,
            "{\"id\": \"session-123\", \"user_id\": \"user-456\"}",
            null,
            "txn-123",
            Instant.now(),
            null
        );
    }
}
