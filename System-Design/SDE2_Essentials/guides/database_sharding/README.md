## Database Sharding

### What and when
* Split a dataset across multiple storage nodes to scale capacity and throughput
* Use when a single primary cannot meet read write or storage demands

### Core strategies
* Range based
* Hash based
* Directory or lookup service

### Key design choices
* Shard key selection and cardinality
* Rebalancing and resharding without downtime
* Hotspot avoidance and load distribution
* Transactions and consistency expectations per shard
* Backfill and dual write during migrations

### Java reference implementation

Shard aware routing with Spring DataSource routing, consistent hash ring, and per shard migrations.

```java
public interface ShardKeyResolver<T> {
    String resolveShardKey(T entityOrKey);
}
```

```java
public interface ShardSelector {
    String selectShardId(String shardKey);
}
```

```java
public final class ConsistentHashSelector implements ShardSelector {
    private final SortedMap<Long, String> ring = new TreeMap<>();
    private final int virtualNodesPerShard;
    private final MessageDigest digest;

    public ConsistentHashSelector(Map<String, DataSource> shardIdToDs, int virtualNodesPerShard) {
        this.virtualNodesPerShard = virtualNodesPerShard;
        this.digest = initDigest();
        for (String shardId : shardIdToDs.keySet()) {
            addShard(shardId);
        }
    }

    private MessageDigest initDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Digest not available", e);
        }
    }

    public void addShard(String shardId) {
        for (int i = 0; i < virtualNodesPerShard; i++) {
            long hash = hashLong(shardId + "#" + i);
            ring.put(hash, shardId);
        }
    }

    public void removeShard(String shardId) {
        ring.entrySet().removeIf(e -> e.getValue().equals(shardId));
    }

    @Override
    public String selectShardId(String shardKey) {
        if (ring.isEmpty()) {
            throw new IllegalStateException("No shards configured");
        }
        long keyHash = hashLong(shardKey);
        SortedMap<Long, String> tail = ring.tailMap(keyHash);
        Long node = tail.isEmpty() ? ring.firstKey() : tail.firstKey();
        return ring.get(node);
    }

    private long hashLong(String value) {
        byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        return buf.getLong();
    }
}
```

```java
public final class RoutingDataSource extends AbstractRoutingDataSource {
    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    public static void setCurrentShardId(String shardId) {
        CONTEXT.set(shardId);
    }

    public static void clear() {
        CONTEXT.remove();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return CONTEXT.get();
    }
}
```

```java
@Configuration
public class ShardingConfig {
    @Bean
    public DataSource dataSource(Map<String, DataSource> shardIdToDs) {
        RoutingDataSource routing = new RoutingDataSource();
        routing.setTargetDataSources(new HashMap<>(shardIdToDs));
        routing.setDefaultTargetDataSource(shardIdToDs.values().iterator().next());
        routing.afterPropertiesSet();
        return routing;
    }
}
```

```java
@Component
public class ShardRouter {
    private final ShardSelector selector;

    public ShardRouter(ShardSelector selector) {
        this.selector = selector;
    }

    public <T> <R> R route(String shardKey, Supplier<R> action) {
        String shardId = selector.selectShardId(shardKey);
        try {
            RoutingDataSource.setCurrentShardId(shardId);
            return action.get();
        } finally {
            RoutingDataSource.clear();
        }
    }
}
```

```java
@Repository
public class AccountRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ShardRouter shardRouter;

    public AccountRepository(JdbcTemplate jdbcTemplate, ShardRouter shardRouter) {
        this.jdbcTemplate = jdbcTemplate;
        this.shardRouter = shardRouter;
    }

    public Account findById(String accountId) {
        return shardRouter.route(accountId, () ->
                jdbcTemplate.queryForObject(
                        "select id, balance from account where id = ?",
                        new BeanPropertyRowMapper<>(Account.class),
                        accountId
                )
        );
    }
}
```

### Schema migration per shard
* Keep one schema folder per shard or use placeholders with Flyway
* Run migrations per shard instance on startup or through an orchestrated job

### Online resharding
* Introduce new ring with additional shards
* Dual write for a bounded period with idempotent upserts
* Backfill with change data capture
* Cut over with a flag when lag reaches zero

### Observability
* Emit metrics per shard for qps latency errors saturation
* Include shard id in logs and traces

### Review checklist
* Shard key stability and cardinality validated
* Skew tests and hotspot detection present
* Backfill plan and dual write safety verified
* Error paths and fallbacks tested
* Runbooks for shard loss and recovery


