# Failover - Netflix Production Guide

## 🎯 Concept overview

Failover is automatic redirection of traffic/workloads upon failure of a component, instance, or region to maintain availability.

## 📊 Implementation layer classification

| Component | Layer | Type | Netflix status |
|---|---|---|---|
| Instance Failover | Application + Infrastructure | LB ejection, retries | Production |
| Data Failover | Infrastructure | replica promotion | Production |
| Regional Failover | Infrastructure | DNS/gateway steering | Production |

## 🚀 Production implementations

- Load balancers eject unhealthy instances; clients retry with budgets
- Databases promote replicas via consensus or orchestrators
- DNS/gateway weighted routing for regional evacuation

## 🧭 Production Readiness Addendum

### Techniques and where to use
- Instance-level: health checks + circuit breaker + retries with backoff
- Data: synchronous replication for RPO=0, async for lower latency
- Regional: warm capacity in secondary, traffic ramps via DNS/gateway

### Trade-offs
- Latency: sync replication adds write latency; cross-region RTT
- Network: replication bandwidth; failover traffic spikes
- Process: split brain risk; fencing and single-writer enforcement
- OS: file descriptor and connection storms on failover; pre-warming
- Cost: warm standby capacity tax
- Complexity: runbooks and automation for orchestration

### Quantified trade offs
* Instance failover: health check interval 2 to 5 s with 2 consecutive failures yields 4 to 10 s ejection. P99 recovery under 30 s with autoscale replacement.
* DB sync failover: quorum adds 1 to 5 ms intra AZ per write; failover RTO 5 to 30 s with orchestrators. Async RPO equals replica lag, typically 10 ms to seconds.
* Regional failover: DNS TTL 30 to 60 s plus client resolver caching leads to 30 to 120 s traffic shift. Gateway weights allow sub second flips but risk overload without warm capacity.
* Retry budgets: cap at 1 to 2 additional attempts with exponential backoff and 10 to 30 percent jitter to avoid storms. Keep caller p99 under SLO during failover drills.

### Failure modes and mitigations
- Flapping: hysteresis, grace windows, disable auto-failback
- Stale secondaries: RPO>0; reconcile via CDC
- Retry storms: client retry budgets, jitter, hedging limits

### Sizing and capacity
- Maintain N+1 or regional 50% headroom to absorb failover
- Pre-provision DNS/gateway quotas and LB capacity

### Verification
- GameDays: AZ/region blackhole tests; database failover drills
- Measure RTO/RPO vs objectives

### Production checklist
- Metrics: failover events, RTO/RPO, error rate spikes, retry budgets
- Alerts: replica lag, health ejections, regional error surges
- Runbooks: promote replica, flip DNS weights, rollback

## 📊 Technique Trade-offs Matrix (Internal)

| Technique | Latency | RPO | RTO | Cost | Complexity | Notes |
|---|---|---|---|---|---|---|
| Instance failover | low | n/a | low | low | low | LB ejection |
| DB sync failover | higher | 0 | low | high | medium | quorum writes |
| DB async failover | low | >0 | low-med | medium | medium | possible data loss |
| Regional DNS steer | low | n/a | med | medium | medium | TTL dependent |

## Deep Dive Appendix

### Adversarial scenarios
- Flapping health causing rapid ejections and churn
- Split brain in data tier during promotion causing divergent histories
- Cascading retries overwhelming healthy shards during regional evacuations

### Internal architecture notes
- Multi layer failover: instance, shard, and regional steering via DNS and gateway weights
- Lag aware routing for replicas; write fences and single writer enforcement
- Retry budgets and backoff integrated with circuit breakers and load shedding

### Validation and references
- GameDays: AZ and region blackholes, database failover drills with measured RTO RPO
- Packet loss and latency injection on replication links
- Literature on quorum systems and failover orchestration

### Trade offs revisited
- RPO vs write latency for sync vs async replication; traffic shifting speed vs overload risk

### Implementation guidance
- Maintain warm capacity and surge limits; precomputed failover weights
- Automate promotion with fencing and verification; disable auto failback unless safe
