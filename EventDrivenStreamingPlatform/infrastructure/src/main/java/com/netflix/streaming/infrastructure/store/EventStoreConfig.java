package com.netflix.streaming.infrastructure.store;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * Event Store Configuration.
 * Provides append-only event storage with replay capabilities.
 */
@Configuration
public class EventStoreConfig {

    @Value("${app.event-store.table-name:event_store}")
    private String eventStoreTableName;

    @Value("${app.event-store.snapshot-table-name:event_snapshots}")
    private String snapshotTableName;

    @Bean
    public EventStore eventStore(JdbcTemplate jdbcTemplate,
                                PlatformTransactionManager transactionManager) {
        return new PostgreSQLEventStore(
            jdbcTemplate,
            transactionManager,
            eventStoreTableName,
            snapshotTableName
        );
    }

    @Bean
    public EventReplayService eventReplayService(EventStore eventStore) {
        return new EventReplayService(eventStore);
    }

    @Bean
    public EventSnapshotService eventSnapshotService(EventStore eventStore) {
        return new EventSnapshotService(eventStore);
    }
}