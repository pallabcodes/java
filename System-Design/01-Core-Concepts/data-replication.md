# Data Replication - Netflix Production Guide

## 🎯 Concept overview

Replication maintains multiple copies of data to improve availability, durability, and locality for reads.

## 📊 Implementation layer classification

| Mode | Layer | Type | Netflix status |
|---|---|---|---|
| Synchronous | Infrastructure | quorum write | Production |
| Asynchronous | Infrastructure | eventual copy | Production |
| Semi sync | Infrastructure | ack after network flush | Production |

## 🚀 Production implementations

- Primary replica with sync replication for zero data loss domains
- Async cross region replication for locality and DR
- Log shipping or change data capture pipelines for projections

## 🧭 Production Readiness Addendum

### Techniques and where to use
- Sync for money moves and invariants where RPO zero is required
- Async for geo read locality and low write latency
- Semi sync as a compromise on latency and safety

### Trade offs
- Latency: sync adds write latency; async minimal impact
- Network: cross region bandwidth and egress
- Process: conflict handling for multi primary models
- OS: disk flush behavior and fsync costs
- Cost: storage and replication streams
- Complexity: topology management and failover sequencing

### Quantified trade offs
* Sync replication: adds 1 to 5 ms intra AZ, 3 to 10 ms cross AZ, and 50 to 100 ms cross region per write at quorum. Throughput drops 20 percent to 50 percent vs async.
* Async replication: typical replica lag 50 ms to multiple seconds under burst. With 1 Gbps links, expect 100 MB per second sustained with compression.
* Semi sync: reduces loss probability to less than 1 write per failover window when ack after network flush, at 10 percent to 20 percent latency overhead.
* Retention: keep change logs for 1 to 24 hours to bridge outages. Storage = ingest_bytes_per_sec × retention_seconds × 1.2.
* Read locality: geo replicated reads save 50 to 150 ms RTT per request but risk staleness; use session tokens to ensure read your writes.

### Failure modes and mitigations
- Replica lag: stale reads; use read tokens or stickiness
- Data loss on failover with async: accept RPO or use fence and recovery
- Divergence in multi primary: conflict resolution policies

### Sizing and capacity
- Provision bandwidth for peak write throughput times replication factor
- Retain logs long enough for catch up and DR

### Verification
- Inject lag and packet loss; validate SLIs and error budgets
- Failover drills with sync and async modes

### Production checklist
- Metrics: replication lag distribution, error rate, bandwidth use
- Alerts: sustained lag, replication link errors, storage near full on replicas
- Runbooks: break and resync, promote replica, rebuild lagging node

## 📊 Technique Trade offs Matrix (Internal)

| Mode | Latency | RPO | RTO | Cost | Complexity | Notes |
|---|---|---|---|---|---|---|
| Sync | higher | zero | low | higher | medium | quorum required
| Async | low | greater than zero | low | medium | low | risk of loss
| Semi sync | medium | near zero | low | medium | medium | compromise

## Deep Dive Appendix

### Adversarial scenarios
- Link throttling and packet loss causing replica lag spikes
- Network partitions leading to divergent histories with async writes
- Clock skew impacting semi sync and failover timing

### Internal architecture notes
- Write ahead log shipping vs logical CDC projections
- Quorum and witness nodes for failover decisions
- Read locality with session tokens and staleness bounds

### Validation and references
- Lag injection and sustained write pressure tests
- Failover and re sync drills with data integrity checks
- Papers on replication protocols and partial synchrony

### Trade offs revisited
- Latency vs RPO; cross region costs vs locality gains

### Implementation guidance
- Set replication factors and retention windows per domain criticality
- Automate rebuilds and ensure safe rejoin procedures
