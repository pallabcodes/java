# Distributed Caching - Netflix Production Guide

## 🎯 Concept overview

Distributed caching shares cached data across instances to reduce backend load and latency at scale, with partitioning and replication for availability.

## 📊 Implementation layer classification

| Component | Layer | Type | Netflix status |
|---|---|---|---|
| Redis Cluster | Application + Infrastructure | Partitioned cache | Production |
| Memcached Pool | Application + Infrastructure | Client-sharded cache | Production |
| CDN Edge | Infrastructure | Global edge cache | Production |

## 🚀 Production implementations

- Redis Cluster with hash slots, pipelining, Lua scripts for atomicity
- Client sharding for Memcached via consistent hashing
- Edge caching via CDN for static/semi-static payloads

## 🧭 Production Readiness Addendum

### Techniques and where to use
- Redis Cluster for dynamic shared data with Lua-based operations
- Memcached for simple KV with large fleets and client-side sharding
- CDN for public content and media

### Trade-offs
- Latency: cross-node operations require careful key colocation
- Network: MOVED/ASK redirects; enable topology refresh
- Process: failover causes slot moves; clients must retry gracefully
- OS: memory fragmentation for large values; use eviction policies appropriately
- Cost: RAM heavy; choose value compression thresholds
- Complexity: cluster management and rebalancing

### Quantified trade offs
* Redis Cluster redirects: MOVED or ASK adds 0.2 to 1 ms per redirected request; enable topology refresh every 5 to 15 s.
* Hot slot: if a slot exceeds 5 percent of total QPS, expect p99 to double; reshard or split keys.
* Memory: plan 30 percent overhead for Redis allocator and metadata; track resident set to avoid swap.
* Pipelining: batch 10 to 50 ops to cut RTT cost by 5x to 20x; measure server queueing under burst.

### Failure modes and mitigations
- Hot slots: reshard, split keys, apply read replicas
- Node loss: redistribute slots; ensure clients handle redirects
- Invalidation storms: jitter TTLs, use event-driven invalidation

### Sizing and capacity
- Slot distribution evenness; value size histograms drive node memory
- Connection pooling per node; pipeline to reduce RTT

### Verification
- Resharding drills in staging; monitor latency and error budgets
- Fault injection: kill node, trigger failover, verify client recovery

### Production checklist
- Metrics: slot skew, node memory, hits/misses, MOVED/ASK rate, P99
- Alerts: node memory high, redirect spikes, failover events
- Runbooks: reshard procedures, node replacement, client cache warming

## 📊 Technique Trade-offs Matrix (Internal)

| Technique | Latency | Availability | Cost | Blast Radius | Complexity | Notes |
|---|---|---|---|---|---|---|
| Redis Cluster | low | high | RAM + net | per slot group | medium | use hash tags and pipelines |
| Memcached client sharding | low | medium | RAM + net | per client mapping | low | simple KV, no replication |
| CDN Edge | lowest | high | egress | per POP | medium | edge invalidation lag |

## Deep Dive Appendix

### Adversarial scenarios
- Hash slot imbalance and hot keys during product launches
- MOVED storm during resharding impacting tail latency
- Serialization changes between services poisoning shared cache

### Internal architecture notes
- Slot aware clients with topology refresh and hash tags for multi key ops
- Background resharding with controlled slot migration rates
- Schema versioning in values and dual write during migrations

### Validation and references
- Resharding drills with synthetic and replay traffic
- Fault injection: node kill, link throttle, packet loss
- Papers on consistent hashing and heavy hitter detection

### Trade offs revisited
- Slot evenness vs operational churn; pipelining depth vs memory

### Implementation guidance
- Define reshard SLOs and playbooks; pre warm new nodes
- Enforce value versioning and compatibility windows
