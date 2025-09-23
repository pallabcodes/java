# Sharding - Production Reference

## Components

- `sharding-shard-key-strategy.java`: Pluggable shard key extraction and stable 64 bit hashing
- `sharding-consistent-hash-shard-router.java`: Consistent hash ring with virtual nodes
- `sharding-shard-manager.java`: HikariCP DataSource lifecycle per shard and replicas
- `sharding-read-write-router.java`: Primary for writes and replica selection for reads
- `sharding-rebalancer.java`: Movement planning for shard set changes
- `sharding-flyway-migrator.java`: Per shard Flyway migrations

## Quick start

```java
ShardKeyStrategy keyStrategy = new ShardKeyStrategy.Default();
ConsistentHashShardRouter router = new ConsistentHashShardRouter(keyStrategy, 128);
router.configureShards(List.of("shard-a","shard-b","shard-c"));

ShardManager shardManager = new ShardManager();
shardManager.configure(List.of(
  new ShardManager.ShardConfig("shard-a","jdbc:postgresql://db-a:5432/app","app","secret", Map.of("maxPool","30"), List.of()),
  new ShardManager.ShardConfig("shard-b","jdbc:postgresql://db-b:5432/app","app","secret", Map.of("maxPool","30"), List.of()),
  new ShardManager.ShardConfig("shard-c","jdbc:postgresql://db-c:5432/app","app","secret", Map.of("maxPool","30"), List.of())
));

ReadWriteRouter rw = new ReadWriteRouter(shardManager, router);
DataSource writeDs = rw.dataSourceForWrite(userId);
DataSource readDs = rw.dataSourceForRead(userId, List.of());
```

## Operational notes

- Choose a shard key that distributes uniformly and is stable over time
- Use virtual nodes to reduce hot spots and smooth rebalancing
- Track per shard pool metrics and error rates; alert on outliers
- Before adding or removing shards, use `ShardingRebalancer` to estimate movement
- Run `ShardingFlywayMigrator` per shard during deploys to keep schemas in sync

## Links

- See `database-design.md` for strategy, best practices, and examples

## Deep Dive Appendix

### Adversarial scenarios
- Hot tenants and skewed shard utilization
- Movement plan errors during reshard causing key loss or duplication
- Mixed version clients during routing table changes

### Internal architecture notes
- Shard map source of truth and router cache invalidation
- Movement planner with preview, safety checks, and phased apply
- Versioned routing and dual writes during transitions

### Validation and references
- Shadow reads and writes during dry runs; checksum and reconciliation
- Replay traffic to validate balance and tail latency
- Literature on consistent hashing and dynamic rebalancing

### Trade offs revisited
- Balance vs movement cost; simplicity vs fine grained control

### Implementation guidance
- Automate detection and remediation; keep movement bounded per window; clear rollback paths


