package com.netflix.systemdesign.sharding;

import org.flywaydb.core.Flyway;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Per-shard Flyway migrator.
 * Usage: register migration locations per shard, then run migrateAll.
 */
public class ShardingFlywayMigrator {
    private final ShardManager shardManager;
    private final Map<String, String> shardLocations; // shardId -> classpath:db/migration/shardX

    public ShardingFlywayMigrator(ShardManager shardManager, Map<String, String> shardLocations) {
        this.shardManager = shardManager;
        this.shardLocations = shardLocations;
    }

    public void migrateAll() {
        for (String shardId : shardManager.shardIds()) {
            migrateShard(shardId);
        }
    }

    public void migrateShard(String shardId) {
        DataSource ds = shardManager.primary(shardId);
        if (ds == null) return;
        String loc = shardLocations.getOrDefault(shardId, "classpath:db/migration");
        Flyway flyway = Flyway.configure()
                .dataSource(ds)
                .locations(loc)
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
    }
}


