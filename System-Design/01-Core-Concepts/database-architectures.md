# Database Architectures - Netflix Production Guide

## 🎯 Concept overview

Database architectures define topology and roles for nodes to meet availability, latency, and scale requirements.

## 📊 Implementation layer classification

| Architecture | Layer | Type | Netflix status |
|---|---|---|---|
| Primary Replica | Infrastructure | single writer, many readers | Production |
| Multi Primary | Infrastructure | multiple writers | Production |
| Shared Nothing Sharded | Application + Infrastructure | partitioned | Production |
| NewSQL/Consensus | Infrastructure | distributed SQL | Production |

## 🚀 Production implementations

- Primary replica for transactional cores with read scaling
- Sharded clusters for horizontal write scale with routing layer
- Consensus based SQL for strong consistency across nodes when needed

## 🧭 Production Readiness Addendum

### Techniques and where to use
- Primary replica when single writer fits domain
- Multi primary when write locality matters and conflicts can be resolved
- Sharding for scale out with clear shard keys
- Distributed SQL for global consistency and transactional guarantees

### Trade offs
- Latency: cross shard or cross region transactions are costly
- Network: replication and consensus traffic increase bandwidth
- Process: schema changes across shards or nodes need orchestration
- OS: resource limits per node; compaction and vacuum effects
- Cost: more nodes and quorum overhead
- Complexity: routing, transaction semantics, conflict resolution

### Quantified trade offs
* Primary replica: replica lag 10 ms to seconds; read your writes requires stickiness for 1 to 5 s after write under burst.
* Multi primary: conflict rate should be under 0.1 percent of writes; conflict resolution adds 5 percent to 30 percent latency for affected keys.
* Sharded: adding a shard moves ~1 divided by n of data; with 10 shards, ~10 percent data move. Plan bandwidth accordingly.
* Distributed SQL: quorum adds 1 to 5 ms intra AZ; cross region transactions add 100 to 300 ms per round trip; limit to small write sets.

### Failure modes and mitigations
- Split brain in multi primary: strong conflict rules and fencing
- Hot shards: rebalance and adaptive routing
- Deadlocks and timeouts in distributed transactions: retry policies

### Sizing and capacity
- Choose shard count and tablet sizes for growth horizon
- Quorum sizing for performance and failure tolerance

### Verification
- Transaction correctness tests under partitions and kill tests
- Schema change drills and rolling upgrades

### Production checklist
- Metrics: per role latency, error rates, hot shard rate, quorum failures
- Alerts: replication lag, consensus timeouts, storage near full
- Runbooks: add shard, reshard, promote writer, resolve conflicts

## 📊 Technique Trade offs Matrix (Internal)

| Architecture | Consistency | Latency | Scale | Cost | Complexity | Notes |
|---|---|---|---|---|---|---|
| Primary Replica | strong | low | read high | medium | low | single writer
| Multi Primary | eventual | low | write high | medium | high | conflict resolution
| Sharded | depends | low | high | medium | medium high | routing layer
| Distributed SQL | strong | medium high | medium | high | high | quorum writes

## Deep Dive Appendix

### Adversarial scenarios
- Replica promotion with stale state causing write loss
- Cross shard transactions deadlocking under high contention
- Split brain in multi primary leading to conflicting writes

### Internal architecture notes
- Router with shard maps, consistent hashing, and range boundaries
- Transaction coordinator for distributed SQL with two phase commit or consensus
- Read after write semantics via session tokens or stickiness

### Validation and references
- Failover drills and Jepsen style tests on transaction invariants
- Load and rebalancing tests at scale
- Literature on Zab, Raft, Spanner, and Calvin

### Trade offs revisited
- Global consistency vs latency; conflict resolution vs availability

### Implementation guidance
- Prefer single writer per shard; apply fencing or version checks on writes
- Automate resharding and metadata updates with safe rollouts
