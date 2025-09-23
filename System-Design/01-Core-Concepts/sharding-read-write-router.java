package com.netflix.systemdesign.sharding;

import javax.sql.DataSource;
import java.util.List;
import java.util.Random;

/**
 * ReadWriteRouter: chooses primary for writes and replica for reads.
 * Replica selection policy: simple random; can be replaced with latency-aware.
 */
public class ReadWriteRouter {
    private final ShardManager shardManager;
    private final ConsistentHashShardRouter shardRouter;
    private final Random random = new Random();

    public ReadWriteRouter(ShardManager shardManager, ConsistentHashShardRouter shardRouter) {
        this.shardManager = shardManager;
        this.shardRouter = shardRouter;
    }

    public DataSource dataSourceForWrite(Object key) {
        String shardId = shardRouter.route(key);
        return shardManager.primary(shardId);
    }

    public DataSource dataSourceForRead(Object key, List<String> replicaIdsOrNull) {
        String shardId = shardRouter.route(key);
        if (replicaIdsOrNull == null || replicaIdsOrNull.isEmpty()) {
            return shardManager.primary(shardId);
        }
        String chosen = replicaIdsOrNull.get(random.nextInt(replicaIdsOrNull.size()));
        DataSource ds = shardManager.replica(shardId, chosen);
        return ds != null ? ds : shardManager.primary(shardId);
    }
}


