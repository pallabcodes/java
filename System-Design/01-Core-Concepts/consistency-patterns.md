# Consistency Patterns - Netflix Production Guide

## 🎯 Concept overview

Consistency defines how reads observe writes across replicas and services. Choose models that balance availability, latency, and correctness for each domain.

## 📊 Implementation layer classification

| Component | Layer | Type | Netflix status |
|---|---|---|---|
| Strong Consistency | Application + Infrastructure | Linearizable ops | Production |
| Eventual Consistency | Application | Convergent state | Production |
| Causal/Session Consistency | Application | Read-your-writes, monotonic reads | Production |
| Saga/Outbox | Application | Distributed transaction patterns | Production |

## 🚀 Production implementations

- Datastore choices: linearizable KV for critical metadata, eventually consistent stores for catalogs
- Outbox + CDC for exactly-once effect in event-driven flows
- Sagas for multi-service workflows with compensations

## 🧭 Production Readiness Addendum

### Techniques and where to use
- Strong for invariants (id uniqueness, payments)
- Session consistency for user-facing read-your-writes
- Eventual for feeds/catalogs; CRDTs for conflict-free counters/sets
- Sagas (choreography/orchestration) for long-running operations

### Trade-offs
- Latency: strong reads are higher latency (quorum) than eventual
- Availability: strong loses availability under partition; eventual remains available
- Network: quorum traffic vs async replication
- Process: compensations complexity in sagas
- Cost: multi-region quorum increases write cost
- Complexity: CRDTs simplify merges but change programming model

### Quantified trade offs
* Strong consistency: quorum writes add 1 to 5 ms intra AZ, 3 to 10 ms cross AZ; cross region quorum can add 50 to 150 ms. Write throughput drops 20 percent to 60 percent vs single leader eventual.
* Session consistency: read your writes tokens add 0.2 to 1 ms per request for token validation and cache lookup.
* Eventual consistency: staleness windows typically equal replication lag, 10 ms to multiple seconds; user visible impact acceptable for feeds and catalogs when below 1 second.
* Saga orchestration: compensation failure rate should be under 1 percent; each compensation doubles total workflow time budget if executed.
* Outbox plus CDC: producer write path adds 0.2 to 1 ms; CDC pipeline latency 50 to 500 ms depending on batch and backpressure.

### Failure modes and mitigations
- Lost updates: use compare-and-set or version checks
- Duplicate events: idempotency keys and dedup stores
- Out-of-order delivery: sequence numbers, vector clocks where needed

### Sizing and capacity
- Quorum write amplification: plan for W+R>N
- CDC/outbox throughput sized to peak write traffic with buffering

### Verification
- Jepsen-inspired tests for invariants; chaos on replication links
- Property tests for saga compensations

### Production checklist
- Metrics: staleness lag, replication latency, outbox backlog, compensation rate
- Alerts: replication lag thresholds, compensation failures
- Runbooks: promote leader, drain backlog, replay outbox

## 📊 Technique Trade-offs Matrix (Internal)

| Technique | Latency | Availability | Cost | Blast Radius | Complexity | Notes |
|---|---|---|---|---|---|---|
| Strong (quorum) | higher | lower | higher | per shard | medium | linearizable invariants |
| Session | low | high | low | per session | low | read-your-writes |
| Eventual | lowest | highest | lowest | per keyspace | low | converges over time |
| Saga | varies | high | medium | per workflow | high | compensations required |
| Outbox+CDC | low | high | medium | per service | medium | exactly-once effect downstream |

## Deep Dive Appendix

### Adversarial scenarios
- Network partitions creating write write conflicts across regions
- Replay and duplicate event deliveries in eventual systems
- Read your writes violations due to replica lag

### Internal architecture notes
- Saga orchestrator with compensations and idempotent handlers
- Outbox CDC pipelines with ordering guarantees per aggregate
- Session tokens and monotonic reads for user flows

### Validation and references
- Property based tests for invariants and compensations
- Jepsen style tests for partitions and recovery
- Literature on CRDTs, Sagas, and consistency models

### Trade offs revisited
- Availability vs transactional guarantees; compensation complexity vs simplicity

### Implementation guidance
- Define invariants and choose minimal consistency required per flow
- Adopt standard libraries for sagas and outbox; document failure semantics
