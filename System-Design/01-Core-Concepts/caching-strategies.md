# Caching Strategies - Netflix Production Guide

## 🎯 Concept overview

Strategies for reading and writing caches to balance latency, freshness, and backend load.

## 📊 Implementation layer classification

| Strategy | Layer | Type | Netflix status |
|---|---|---|---|
| Cache aside | Application | lazy load | Production |
| Write through | Application | sync write | Production |
| Write behind | Application | async write | Production |
| Refresh ahead | Application | proactive refresh | Production |

## 🚀 Production implementations

See `caching.md` for Redis, Memcached, CDN, and L1 implementations and patterns.

## 🧭 Production Readiness Addendum

### Techniques and where to use
- Cache aside for simple read heavy domains and ownership in app
- Write through for correctness sensitive data where stale reads are unacceptable
- Write behind for high write throughput and tolerant domains with durable queue
- Refresh ahead for predictable hot keys and user facing low tail

### Trade offs
- Consistency vs freshness: write through strongest, write behind weakest until flush
- Latency: write through adds write cost; cache aside hits backend on miss
- Network: refresh ahead and invalidation amplify background traffic
- Process: failure handling with write behind requires replay and compaction
- Cost: background compute and storage for queues and refreshers
- Complexity: multi tier invalidation and versioning

### Quantified trade offs
- Cache aside: miss penalty equals origin latency; with 80 percent hit rate, origin load drops 5x. At 95 percent, drops 20x.
- Write through: per write overhead 0.5 to 2 ms to update cache intra AZ. Adds 10 percent to 30 percent CPU on write path.
- Write behind: queue backlog budget = write_qps × worst_case_downtime. Keep backlog drain capacity at 2x peak to recover within SLO.
- Refresh ahead: refresh lead 5 percent to 15 percent of TTL; background QPS ≈ hot_keys / lead_window. Aim for P99 improvement 20 percent to 50 percent on hot sets.

### Failure modes and mitigations
- Stampede on cold start: stagger warmup, use single flight
- Lost writes in write behind: durable queue with idempotent apply and replay
- Stale data with cache aside: short TTL for sensitive data and read your writes via session stickiness

### Sizing and capacity
- Hit rate target sets required memory: memory = entries × (value + overhead)
- Background workers sized from refresh and write behind throughput goals

### Verification
- Replay real traces to measure hit rate and tail improvements
- Inject cache node loss and queue delays

### Production checklist
- Metrics: hit/miss, put rate, refresh rate, backlog depth, error rate
- Alerts: hit rate dips, backlog breach, refresh failures
- Runbooks: stampede mitigation, replay backlog, widen TTLs temporarily

## 📊 Technique Trade offs Matrix (Internal)

| Strategy | Latency | Freshness | Cost | Complexity | Notes |
|---|---|---|---|---|---|
| Cache aside | low on hit | stale by TTL | low | low | simple ownership |
| Write through | adds on write | strongest | medium | medium | correctness first |
| Write behind | hides write | weaker until flush | medium | high | durable queue needed |
| Refresh ahead | smoothest | good | medium | medium | jitter and budgets |

## Deep Dive Appendix

### Adversarial scenarios
- Boundary effects in fixed windows and stampede during mass expiry
- Dual writes drift in write behind under backlog pressure
- Prewarm errors leading to thundering herds on release

### Internal architecture notes
- Hierarchical enforcement of cache aside, write through, write behind, refresh ahead
- Durable queues with idempotent apply for write behind
- Predicted refresh based on access heat and decay models

### Validation and references
- Replay traces comparing strategies on hit rate and backend load
- Queue pause injection and recovery drills
- Literature on caching algorithms and refresh policies

### Trade offs revisited
- Freshness, latency, and backend amplification across strategies

### Implementation guidance
- Paved road modules and defaults per domain; safe toggles and rollbacks
- Monitoring dashboards focused on hit rate, stampede, and backlog depth
