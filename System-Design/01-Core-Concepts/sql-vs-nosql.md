# SQL vs NoSQL - Netflix Production Guide

## 🎯 Concept overview

Compare relational (SQL) and non-relational (NoSQL) systems to align data models, consistency needs, and scale characteristics.

## 📊 Implementation layer classification

| Model | Layer | Characteristics | Netflix status |
|---|---|---|---|
| SQL | Infrastructure | ACID, joins, schemas | Production |
| NoSQL | Infrastructure | Scale-out, flexible schemas | Production |

## 🚀 Production implementations

- SQL: Postgres/MySQL for transactional workloads
- NoSQL: Cassandra/Bigtable/Redis for scalable reads/writes and flexible models

## 🧭 Production Readiness Addendum

### Techniques and where to use
- SQL for transactions, referential integrity, complex joins
- NoSQL for high write/read throughput, flexible documents, wide-column patterns
- Hybrid: use SQL for source of truth, NoSQL for derived views

### Trade-offs
- Latency: SQL depends on indexes; NoSQL optimized for partitioned access
- Availability: CAP trade-offs; NoSQL often AP/CP; SQL typically CP with HA
- Network: cross-partition joins expensive; prefer precomputed views
- Process: schema migrations vs schema-on-read
- OS: IO patterns; compaction costs in LSM-based NoSQL
- Cost: scale-up vs scale-out; storage amplification in NoSQL
- Complexity: denormalization shifts complexity to application

### Quantified trade offs
* SQL: cross table joins on millions of rows can add seconds without proper indexes; transactions with serializable isolation add 20 percent to 100 percent overhead vs read committed.
* NoSQL: partition fan out greater than 2 to 3 partitions per query grows latency near linearly; hot key exceeding 1 percent of traffic causes p99 to explode.
* Cost: scale up RDBMS licensing and hardware premium vs scale out commodity for NoSQL; storage amplification 2x to 3x in LSM based stores.
* Consistency: quorum reads writes add 1 to 5 ms intra AZ; global consistency adds 50 to 150 ms RTT per write.

### Failure modes and mitigations
- Write hotspots: adjust keys, shard; use batch writes
- Stale views: TTL and refresh pipelines
- Transactional gaps: use Sagas/outbox for cross-service consistency

### Sizing and capacity
- SQL: index sizes vs RAM; connection pool sizing; write-ahead log throughput
- NoSQL: partition count vs QPS; replication factor; compaction overhead

### Verification
- Query plans and latencies under load; consistency checks across projections
- Chaos: node loss, network partitions; validate desired model (CP/AP) behavior

### Production checklist
- Metrics: p50/p99 per query/operation, error rate, compaction/GC, hot partitions
- Alerts: replication lag, error spikes, storage near-full
- Runbooks: resharding, index rebuilds, projection rebuild

## 📊 Technique Trade-offs Matrix (Internal)

| Model | Latency | Consistency | Cost | Blast Radius | Complexity | Notes |
|---|---|---|---|---|---|---|
| SQL | medium | strong | medium | per primary | medium | ACID strength |
| NoSQL | low-med | eventual/strong (vendor) | low-med | per partition | medium | scale-out |

## Deep Dive Appendix

### Adversarial scenarios
- Multi partition fan out queries causing cascading timeouts
- Denormalized documents drifting from source of truth
- Global writes with strict consistency exceeding latency budgets

### Internal architecture notes
- CQRS boundaries with outbox CDC to build NoSQL projections
- Schema evolution policies and versioned documents
- Transaction isolation and lock contention models

### Validation and references
- Compare query plans and latencies across models with replay traces
- Failure injection on partitions, leaders, and cross region links
- Literature on distributed transactions and eventual consistency

### Trade offs revisited
- Developer velocity vs operational complexity; consistency vs availability

### Implementation guidance
- Keep write path authoritative in SQL where correctness demands; project to NoSQL for scale
- Establish schema registries and contract tests for projections
