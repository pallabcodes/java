package com.netflix.systemdesign.sharding;

import java.util.*;

/**
 * ShardingRebalancer: scaffolding for planning key migration when shard set changes.
 * It computes which shard ids gain or lose responsibility when the router's ring changes.
 */
public class ShardingRebalancer {
    private final ConsistentHashShardRouter router;

    public ShardingRebalancer(ConsistentHashShardRouter router) {
        this.router = router;
    }

    /**
     * Given a sample of keys and a proposed shard set, compute movement plan.
     */
    public MovementPlan planMovement(Collection<String> sampleKeys, List<String> newShardIds) {
        Map<String, Integer> fromCounts = new HashMap<>();
        Map<String, Integer> toCounts = new HashMap<>();
        int moved = 0;

        // Clone router and reconfigure for prediction
        ConsistentHashShardRouter prediction = new ConsistentHashShardRouter(new ShardKeyStrategy.Default());
        prediction.configureShards(newShardIds);

        for (String k : sampleKeys) {
            String from = router.route(k);
            String to = prediction.route(k);
            fromCounts.merge(from, 1, Integer::sum);
            toCounts.merge(to, 1, Integer::sum);
            if (!Objects.equals(from, to)) moved++;
        }
        return new MovementPlan(sampleKeys.size(), moved, fromCounts, toCounts);
    }

    public static class MovementPlan {
        public final int totalSampled;
        public final int keysMoved;
        public final Map<String, Integer> fromDistribution;
        public final Map<String, Integer> toDistribution;
        public MovementPlan(int totalSampled, int keysMoved,
                            Map<String, Integer> fromDistribution,
                            Map<String, Integer> toDistribution) {
            this.totalSampled = totalSampled;
            this.keysMoved = keysMoved;
            this.fromDistribution = Collections.unmodifiableMap(new HashMap<>(fromDistribution));
            this.toDistribution = Collections.unmodifiableMap(new HashMap<>(toDistribution));
        }
    }
}


