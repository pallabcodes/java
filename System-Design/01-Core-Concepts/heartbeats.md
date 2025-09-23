# HeartBeats - Netflix Production Guide

## 🎯 Concept overview

Heartbeats are periodic liveness signals used to detect failures, measure health, and drive failover.

## 📊 Implementation layer classification

| Component | Layer | Type | Netflix status |
|---|---|---|---|
| Instance Heartbeat | Application | Push/pull liveness | Production |
| Leader Heartbeat | Application + Infrastructure | Leader lease | Production |
| DB/Cache Heartbeat | Infrastructure | Dependency health | Production |

## 🚀 Production implementations

- Push heartbeats to control plane (Eureka/Consul) and pull checks from load balancers
- Leader leases using consensus (etcd/ZK) with TTL and renewal
- Health endpoints with synthetic checks

## 🧭 Production Readiness Addendum

### Techniques and where to use
- Push for highly dynamic fleets; pull for ingress routing decisions
- Leader heartbeat with short TTL for fast failover
- Multi-signal health: CPU, GC, error rates, dependency checks

### Trade-offs
- Latency: shorter intervals detect failures faster but add load
- Network: many nodes emitting heartbeats; batch and compress
- Process: GC pauses cause missed beats; require grace windows
- OS: clock skew; prefer monotonic timers
- Cost: control-plane ingestion at scale
- Complexity: avoid health flapping with hysteresis

### Quantified trade offs
* Interval vs detection time: with interval I and miss threshold M, detection ≈ M × I. Typical I 2 to 5 s and M 2 to 3 gives 4 to 15 s detection.
* Control plane load: for N instances, heartbeat QPS ≈ N ÷ I. At 50k instances and I 3 s, ~16.6k QPS. Batch and compress to keep CPU under 20 percent.
* Clock skew and jitter: 100 to 300 ms skew requires grace windows at least 2x skew to avoid false positives.
* Leader lease: TTL 3 to 10 s with renew at 30 percent of TTL yields fast failover without flapping. Probe success rate should exceed 99.9 percent per window.

### Failure modes and mitigations
- False positives: grace periods, multiple consecutive misses
- Split brain leaders: use leases with fencing
- Cascading failures from dependency checks: isolate expensive checks

### Sizing and capacity
- Heartbeat interval vs detection time: target 2–3 intervals to declare down
- Control plane QPS and storage for fleet size

### Verification
- Blackhole tests; stop heartbeat to validate detection and failover times
- Inject GC pauses to ensure grace covers common pauses

### Production checklist
- Metrics: heartbeat latency/jitter, misses, false positives
- Alerts: sustained misses per AZ/cluster, leader re-elections spikes
- Runbooks: increase grace, drain node, trigger failover

## 📊 Technique Trade-offs Matrix (Internal)

| Technique | Detection Time | False Positive Risk | Cost | Blast Radius | Complexity | Notes |
|---|---|---|---|---|---|---|
| Push heartbeat | low | medium | control-plane | per fleet | medium | scale ingestion |
| Pull health check | low | low | LB CPU | per tier | low | ingress-driven |
| Leader lease | lowest | low | consensus | per group | medium | fencing tokens |

## Deep Dive Appendix

### Adversarial scenarios
- GC and scheduler pauses creating false misses
- Anycast resolver or control plane outages dropping heartbeats
- Network jitter and asymmetric delays across AZs

### Internal architecture notes
- Push vs pull hybrid with grace windows and hysteresis
- Leader lease with monotonic clocks and fencing tokens for actions
- Synthetic checks combining liveness and readiness signals

### Validation and references
- Blackhole tests, pause injection, and clock skew simulations
- SRE literature on failure detection and leases

### Trade offs revisited
- Detection speed vs false positive rate; control plane QPS vs interval

### Implementation guidance
- Default intervals and grace tuned per tier; monotonic timers and jitter
- Independent signals and quorum decisions for critical actions
