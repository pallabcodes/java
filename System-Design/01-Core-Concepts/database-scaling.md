# Database Scaling - Netflix Production Guide

## 🎯 Concept overview

Database scaling increases capacity and resilience for reads and writes through vertical scaling, replication, partitioning, and caching.

## 📊 Implementation layer classification

| Technique | Layer | Type | Netflix status |
|---|---|---|---|
| Vertical scale | Infrastructure | Bigger instance | Production |
| Read replicas | Infrastructure | Asynchronous replication | Production |
| Sharding | Application + Infrastructure | Partition by key | Production |
| Caching | Application + Infrastructure | Reduce read load | Production |
| CQRS | Application | Read write separation | Production |

## 🚀 Production implementations

- Primary with N read replicas for fan out reads, with lag aware routing
- Sharding by tenant or key with consistent hashing and rebalancer
- Write through cache for hot reads
- CQRS projector pipelines to read optimized views

## 🧭 Production Readiness Addendum

### Techniques and where to use
- Vertical for quick relief when headroom exists
- Replicas for read heavy workloads with tolerance for lag
- Sharding for write scale and large datasets
- CQRS when read models diverge from write models

### Trade offs
- Latency: cross shard joins are expensive; avoid with denormalized reads
- Network: replication bandwidth and cross AZ traffic
- Process: migrations and resharding require orchestration
- OS: file descriptor and connection limits per shard
- Cost: more nodes, more operational overhead
- Complexity: routing logic, schema evolution across shards

### Quantified trade offs
* Vertical scaling: doubling CPU rarely yields more than 1.6x throughput due to lock contention and IO limits.
* Read replicas: expect 10 to 200 ms replica lag under heavy write bursts. For read your writes, use session stickiness for up to 1 to 5 seconds after write.
* Sharding: cross shard joins increase latency by 2x to 10x. Keep cross shard traffic under 5 percent of total calls.
* Connection limits: plan 50 to 200 active connections per shard and use pooling. Beyond that, context switching dominates.
* Rebalancing cost: moving 1 TB per shard at 200 MB per second takes ~1.5 hours excluding catch up. Perform under traffic drains and with throttling.

### Failure modes and mitigations
- Hot shards: rebalance keys, move tenants, power of two shards
- Replica lag: stale reads; use session consistency or stickiness
- Split brain: single writer with fencing; orchestrated promotions

### Sizing and capacity
- Determine shard count from peak write QPS and storage growth
- Maintain per shard headroom and connection pools

### Verification
- Reshard dry runs and shadow traffic
- Load tests with routing and failure injection

### Production checklist
- Metrics: per shard p99, hot partition rate, replica lag, error rate
- Alerts: lag thresholds, hot shards, storage near full, router errors
- Runbooks: add shard, move tenant, promote replica, rollback

## 📊 Technique Trade offs Matrix (Internal)

| Technique | Read Gain | Write Gain | Consistency | Cost | Complexity | Notes |
|---|---|---|---|---|---|---|
| Vertical | medium | medium | strong | medium | low | quick but limited
| Replicas | high | none | eventual | medium | low | lag aware routing
| Sharding | medium | high | depends | medium high | medium high | routing needed
| Caching | high | none | eventual | medium | low | edge and mid tier
| CQRS | high | medium | eventual | medium | medium | projectors

## Deep Dive Appendix

### Adversarial scenarios
- Hot shard due to skewed keys or tenant growth
- Resharding under load causing timeouts and stale reads
- Replica lag bursts during write spikes

### Internal architecture notes
- Shard manager with placement, routing, and rebalancer
- Lag aware read routers and session stickiness for read your writes
- CQRS projectors to decouple read scaling from write paths

### Validation and references
- Shadow traffic during reshard and migration rehearsals
- Failure injection on routers and shard managers
- Literature on consistent hashing and rebalancing strategies

### Trade offs revisited
- Write scale vs cross shard joins; operational complexity vs flexibility

### Implementation guidance
- Plan shard keys and boundaries early; automate move and split operations
- Keep per shard headroom; monitor hot partition indicators
