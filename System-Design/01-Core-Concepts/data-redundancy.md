# Data Redundancy - Netflix Production Guide

## 🎯 Concept overview

Data redundancy stores additional copies or parity of data to improve durability, availability, and recovery.

## 📊 Implementation layer classification

| Technique | Layer | Type | Netflix status |
|---|---|---|---|
| Replication | Infrastructure | multiple full copies | Production |
| Erasure Coding | Infrastructure | parity blocks | Production |
| Snapshots/Backups | Infrastructure | point-in-time copies | Production |

## 🚀 Production implementations

- Multi AZ and multi region replicas for operational continuity
- Erasure coding in object storage for cost efficient durability
- Periodic snapshots and continuous backups with tested restores

## 🧭 Production Readiness Addendum

### Techniques and where to use
- Replication for low RTO operational failover
- Erasure coding for cold and warm data durability with low cost
- Snapshots and PITR for human error and corruption recovery

### Trade offs
- Latency: replication adds write latency when synchronous
- Network: replication and backup bandwidth; cross region egress
- Process: backup verification and restore drills are mandatory
- OS: storage IO contention during snapshots
- Cost: storage multiplier; erasure coding reduces cost at compute expense
- Complexity: catalog and retention management

### Failure modes and mitigations
- Silent corruption: checksums end to end; periodic scrubbing
- Backup unusable: automated restore tests and checks
- Replica drift: strong consistency or CDC reconcile

### Sizing and capacity
- Replication factor vs durability targets
- Backup retention policy vs storage budget

### Verification
- Regular restore tests with RPO/RTO measurement
- Object integrity audits with checksum verification

### Production checklist
- Metrics: backup success rate, restore time, checksum error rate
- Alerts: failed backups, integrity errors, storage thresholds
- Runbooks: restore from snapshot, region recovery, parity rebuild

## 📊 Technique Trade offs Matrix (Internal)

| Technique | Durability | RTO | Cost | Complexity | Notes |
|---|---|---|---|---|---|
| Replication | very high | low | high | medium | fast failover |
| Erasure coding | very high | medium | medium | medium | cost efficient |
| Snapshots/PITR | high | medium | medium | low | human error recovery |
