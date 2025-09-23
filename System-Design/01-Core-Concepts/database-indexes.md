# Database Indexes - Netflix Production Guide

## 🎯 Concept overview

Indexes accelerate queries by maintaining auxiliary data structures; choose types and columns carefully to balance write cost and read gains.

## 📊 Implementation layer classification

| Index Type | Layer | Use cases | Netflix status |
|---|---|---|---|
| B-tree | Infrastructure | range/equality on ordered columns | Production |
| Hash | Infrastructure | equality lookups | Production |
| GIN/GIST | Infrastructure | full-text, JSONB, geospatial | Production |
| Composite | Infrastructure | multi-column predicates | Production |
| Covering (INCLUDE) | Infrastructure | avoid lookups | Production |

## 🚀 Production implementations

- Postgres B-tree, hash, GIN/GIST indexes; partial and composite indexes
- Covering indexes to eliminate table lookups for hot queries

## 🧭 Production Readiness Addendum

### Techniques and where to use
- Index selective columns used in predicates and joins
- Composite indexes ordered by selectivity and filter order
- Partial indexes for common filtered subsets

### Trade-offs

- Latency: hot queries see 2x to 100x improvement when the predicate is selective and the working set of the index fits memory. If index does not cover the query or causes random IO, expect modest gains only.

- Write amplification: each indexed write incurs 1 extra index write per affected index. As a rule, every additional index increases write CPU by 10 percent to 30 percent on write heavy tables. Measure rows written per second × number of indexes to budget CPU.

- Storage overhead: typical B-tree index size is 30 percent to 120 percent of base table column sizes depending on datatype and fillfactor. Plan total storage = table size + sum(index sizes) + 20 percent headroom for bloat.

- Maintenance windows: heavy update tables with many secondary indexes require more aggressive autovacuum and periodic reindex concurrently. Target autovacuum to keep dead tuples below 10 percent of table pages. Schedule reindex when bloat exceeds 20 
percent.

- Planner stability: too many overlapping indexes increase plan instability. Prefer one composite index per dominant query rather than multiple single column indexes. Track plan regression rate and pin plans only as last resort.

- Cache residency: keep hot portions of the most used indexes in memory. Aim for index hit ratio greater than 95 percent on primary access paths. If hit ratio drops below 90 percent under load, consider covering index or additional RAM.

### Rules of thumb

| Workload | Recommended index strategy | Expected impact |
|---|---|---|
| OLTP mixed reads writes | 1 to 3 well chosen indexes per hot table | 10x read latency reduction, 10 percent to 30 percent higher write CPU |
| Read heavy lookups | covering index on hot query | 2x to 5x lower latency, fewer buffer hits |
| Range scans on time | B-tree on timestamp DESC with partial index on recent window | stable p99, smaller index footprint |
| JSONB search | GIN on specific keys, partial index for common filters | large latency gains, higher write cost |

### Failure modes and mitigations
- Bloat and stale stats: autovacuum tuning, analyze, periodic reindex
- Wrong index chosen: query hints, updated stats, simplified predicates
- Hot updates: fillfactor tuning, HOT updates in Postgres

### Sizing and capacity
- Estimate index size = (key + pointer + overhead) * rows
- Keep hot indexes in memory; plan for cache hit ratios

### Verification
- EXPLAIN/ANALYZE under load; tracking plan regressions
- Canary index addition before full rollout

### Production checklist
- Metrics: index hit ratio, bloat, autovacuum activity, write amplification
- Alerts: bloat thresholds, long-running VACUUM, slow queries
- Runbooks: reindex concurrently, add/drop index, statistics refresh

## 📊 Technique Trade-offs Matrix (Internal)

| Index | Read Latency | Write Cost | Storage | Complexity | Notes |
|---|---|---|---|---|---|
| B-tree | low | medium | medium | low | default general-purpose |
| Hash | low | low | medium | low | equality only |
| GIN/GIST | low | high | high | medium | specialized workloads |
| Composite | low | medium | medium | medium | order matters |
| Covering | lowest | medium | higher | medium | avoid table lookups |

## Deep Dive Appendix

### Adversarial scenarios
- Bloat and stale stats leading to plan regressions
- Hot updates causing page splits and lock contention
- Partial index misuse causing missed plans

### Internal architecture notes
- Selectivity driven composite index design and covering indexes
- Partial indexes for common filtered subsets and recent time windows
- Autovacuum and analyze tuning; reindex concurrently procedures

### Validation and references
- EXPLAIN ANALYZE under load and plan regression tracking
- Benchmarks with realistic data and skew; index-only scan validation
- Literature on B trees, GIN GIST, and optimizer behaviors

### Trade offs revisited
- Read latency vs write amplification vs storage overhead

### Implementation guidance
- Keep index set minimal and focused on dominant queries
- Periodically review unused indexes and remove safely
