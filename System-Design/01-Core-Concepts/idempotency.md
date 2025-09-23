# Idempotency - Netflix Production Guide

## 🎯 Concept overview

Idempotency ensures repeated requests have the same effect as a single request, critical for retries and exactly-once effects at the edges.

## 📊 Implementation layer classification

| Component | Layer | Type | Netflix status |
|---|---|---|---|
| Idempotency Keys | Application | Request de-duplication | Production |
| Outbox/Inbox | Application | Message dedup | Production |
| Conditional Writes | Application + Infrastructure | CAS/version checks | Production |

## 🚀 Production implementations

- Idempotency-Key header with request hash and TTL-backed store
- Outbox (producer) and Inbox (consumer) to dedup events
- CAS/ETag on updates for lost-update protection

## 🧭 Production Readiness Addendum

### Techniques and where to use
- Keys for API create operations (payments, orders)
- Inbox/outbox for messaging and event processing
- CAS for resource updates and inventory decrements

### Trade-offs
- Latency: key lookups add small overhead
- Network: distributed key store; choose locality
- Process: key TTL tuning; avoid premature expiry
- OS: storage IO; compact dedup tables
- Cost: storage for key retention window
- Complexity: hashing canonical requests, payload stability

### Quantified trade offs
* Key store overhead: per request key check adds 0.2 to 1 ms intra AZ with Redis or KV. Storage sizing: entries_per_sec × retention_seconds × entry_size.
* False positives: use UUID v4 or SHA256 of canonical request; collision probability negligible for retention windows under days. Include schema version to avoid drift.
* TTL: set to 2x to 10x of client retry window. For payment APIs with 24h retries, TTL at 24 to 48h; for internal services, 5 to 30 minutes.
* Outbox inbox: storage growth equals produce_rate × retention_window; compaction jobs should drain at 2x peak to maintain under 70 percent capacity.

### Failure modes and mitigations
- Key collisions: use UUID v4 or strong hash + namespace
- Payload drift: canonicalization strategy; include schema version
- Store outage: fail closed for critical ops; queue and retry later

### Sizing and capacity
- Key store capacity = RPS * retention_window * entry_size
- Partition keys by tenant to avoid hotspots

### Verification
- Replay traffic to validate de-dup; fuzz tests for key generation
- Chaos: drop key store, ensure safe behavior

### Production checklist
- Metrics: dedup hit rate, false positive rate, store latency
- Alerts: store latency spikes, error rates, capacity thresholds
- Runbooks: rotate namespaces, rebuild dedup table, extend retention

## 📊 Technique Trade-offs Matrix (Internal)

| Technique | Latency | Reliability | Cost | Blast Radius | Complexity | Notes |
|---|---|---|---|---|---|---|
| Idempotency Key Store | low | high | storage | per keyspace | medium | TTL + namespace |
| Outbox/Inbox | low | high | storage | per service | medium | dedup with offsets |
| CAS/ETag | low | high | minimal | per resource | low | protects lost updates |

## Deep Dive Appendix

### Adversarial scenarios
- Duplicate submissions due to client retries and network retries
- Reordered deliveries in asynchronous pipelines
- Hash collisions or schema drift causing mis deduplication

### Internal architecture notes
- Key derivation from canonical request with namespace and version
- Inbox tables with unique constraints and dedup tokens in consumers
- CAS version checks and ETag for HTTP semantics

### Validation and references
- Replay traffic with duplicates and reorders to verify effect
- Fuzz tests on canonicalization and hashing
- Literature on exactly once effect and idempotent design

### Trade offs revisited
- Storage and TTL for dedup vs reliability of retries

### Implementation guidance
- Standardize headers and key formats; document retention windows
- Provide paved road libraries for key storage and dedup checks
