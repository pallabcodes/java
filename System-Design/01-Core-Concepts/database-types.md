# Database Types - Netflix Production Guide

## 🎯 Concept overview

Choose the right database paradigm per workload: relational, document, key-value, wide-column, time series, graph, search.

## 📊 Implementation layer classification

| Type | Layer | Use cases | Netflix status |
|---|---|---|---|
| Relational (SQL) | Infrastructure | transactions, joins, strong consistency | Production |
| Document | Infrastructure | flexible schemas, content/user profiles | Production |
| Key-Value | Infrastructure | metadata, configs, sessions | Production |
| Wide-column | Infrastructure | analytical, time/range scans | Production |
| Time series | Infrastructure | metrics, events | Production |
| Graph | Infrastructure | relationships, recommendations | Production |
| Search/Index | Infrastructure | text search, aggregations | Production |

## 🚀 Production implementations

- Postgres/MySQL for transactional core
- Cassandra/Bigtable for large-scale time/range workloads
- Elasticsearch/OpenSearch for search
- Redis/KV for metadata and caching

## 🧭 Production Readiness Addendum

### Techniques and where to use
- Relational for correctness and complex joins
- NoSQL for scale and flexible models; denormalize reads
- Polyglot persistence per domain

### Trade-offs
- Latency: KV lowest, relational depends on indexes and joins
- Availability: quorum stores vs primary-replica
- Network: cross-AZ replication costs
- Process: schema migrations vs schema-less evolution
- OS: storage IO patterns (random vs sequential)
- Cost: storage + compute; hot partitions increase spend
- Complexity: multi-model introduces operational overhead

### Quantified trade offs
* Relational: complex joins on hot path add 2x to 20x latency vs indexed lookups. Single primary write throughput typically caps at tens of thousands TPS without sharding.
* Document stores: secondary index writes add 20 percent to 60 percent write overhead; sparse indexes reduce cost for selective fields.
* KV stores: p99 read 1 to 5 ms intra region; with quorum reads add 1 to 3 ms. Hot partition above 5 percent of traffic triggers throttling and tail growth.
* Wide column: compaction consumes 10 percent to 30 percent IO; write amplification 5x to 20x for LSM designs; tune tombstone GC windows.
* Search: indexing pipelines add 100 ms to seconds; refresh intervals 1 to 5 s trade freshness for throughput.

### Failure modes and mitigations
- Hot partition: rebalance/shard, adjust keys
- Replication lag: read-after-write issues; use session tokens
- Index bloat: tune indexes, vacuum/reindex

### Sizing and capacity
- Working set vs RAM; index sizes; write/read QPS
- Throughput model: W+R capacity per shard

### Verification
- Load tests with representative queries; migration dry-runs
- Chaos on replicas and leaders

### Production checklist
- Metrics: p99 latency per query, replication lag, hot partition rate
- Alerts: error spikes, lag thresholds, storage near-full
- Runbooks: failover, reshard, index maintenance

## 📊 Technique Trade-offs Matrix (Internal)

| Type | Latency | Consistency | Cost | Blast Radius | Complexity | Notes |
|---|---|---|---|---|---|---|
| Relational | medium | strong | medium | per primary | medium | ACID, transactions |
| Document | low-med | eventual/strong (vendor) | medium | per shard | medium | flexible schema |
| KV | low | eventual/strong (vendor) | low | per partition | low | simple model |
| Wide-column | low-med | eventual | medium | per tablet | medium | range scans |
| Time series | low | eventual | medium | per retention | medium | rollups |
| Graph | med-high | strong | high | per cluster | high | traversals |
| Search | med-high | eventual | high | per index | high | inverted index |

## Deep Dive Appendix

### Adversarial scenarios
- Hot partition in KV causing tail inflation and throttling
- Secondary index bloat and compaction storms in wide column stores
- Full text index refresh delays causing stale search results

### Internal architecture notes
- Selection matrix mapping domain access patterns to storage models
- Hybrid projections: SQL source of truth with NoSQL read models via CDC
- Quorum and replication settings per workload and region

### Validation and references
- Load tests with representative queries and data distributions
- Chaos on replicas, leaders, and compaction throttling
- Papers on LSM trees, B trees, and CAP implications

### Trade offs revisited
- Latency and consistency budgets vs operational cost and complexity
- Partitioning and sharding boundaries vs query flexibility

### Implementation guidance
- Start with simplest model that meets SLOs; introduce projections when necessary
- Document per domain storage expectations and growth horizons
