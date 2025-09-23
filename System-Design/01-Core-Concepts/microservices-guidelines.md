# Microservices Guidelines - Netflix Production Guide

## 🎯 Concept overview

Opinionated guardrails for building resilient, observable, secure, and scalable services.

## 📊 Implementation layer classification

| Area | Layer | Scope | Netflix status |
|---|---|---|---|
| API and contracts | Application | REST gRPC GraphQL | Production |
| Resilience | Application | retries timeouts circuit breaker | Production |
| Security | Application + Infrastructure | authn authz mtls | Production |
| Observability | Application + Infrastructure | logs metrics traces | Production |
| Delivery | Application + Infrastructure | CI CD canaries | Production |

## 🚀 Production implementations

- Contract first APIs with versioning and backward compatibility
- Timeouts and retries with budgets, circuit breakers, bulkheads
- mTLS with SPIFFE identities, fine grained RBAC
- OpenTelemetry metrics logs traces with exemplars and alert rules
- Progressive delivery with canaries and automated rollback

## 🧭 Production Readiness Addendum

### Techniques and where to use
- Choose API style per domain, enforce breaking change policy
- Budget based retries, hedging, and backpressure
- Encrypt everywhere and minimize secrets blast radius
- Golden signals and SLO based alerting
- Blue green and canary rollouts with feature flags

### Trade offs
- Latency: resilience features add small overhead but reduce tail risk
- Network: egress controls and rate limits contain blast radius
- Process: stricter change management increases safety
- OS: resource isolation and quotas prevent noisy neighbors
- Cost: observability storage, canary duplication
- Complexity: platform paved road reduces developer burden

### Quantified trade offs
* Retries: cap at 1 to 2 with backoff 50 to 200 ms base; beyond that increases tail more than success rate. Retry budget under 10 percent of total traffic.
* Timeouts: set to P99.5 of dependency + 10 to 30 percent headroom; never exceed caller SLO. End to end budget sliced per hop.
* Circuit breaker thresholds: minimum volume 50 to 200 per window; failure rate 20 percent to 50 percent depending on fallback quality; open for 10 to 60 s.
* Observability cost: full trace sampling at 100 percent adds 1 to 5 percent CPU and storage; default 1 to 10 percent, 100 percent during incidents.

### Failure modes and mitigations
- Retry storms: budgets and jitter
- Cascading failures: circuit breakers and load shedding
- Inconsistent contracts: contract tests and schema registry

### Sizing and capacity
- Scale from RPS and payload sizes; set pool and queue limits
- Define per tenant limits and quotas

### Verification
- Chaos experiments, fault injection, capacity tests
- Synthetic checks and conformance tests in CI

### Production checklist
- Metrics: error budget burn, p99 latency, saturation, retry budget
- Alerts: SLO breaches, dependency health, saturation
- Runbooks: shed load, degrade features, rollback

## 📊 Technique Trade offs Matrix (Internal)

| Area | Latency | Reliability | Cost | Complexity | Notes |
|---|---|---|---|---|---|
| Resilience | low | high | low | medium | budgets and breakers |
| Security | low | high | medium | medium | mtls and RBAC |
| Observability | low | high | medium | low | exemplars and alerts |
| Delivery | low | high | medium | medium | canaries and flags |
| API | low | high | low | medium | contract first and versioned |

## Deep Dive Appendix

### Adversarial scenarios
- Retry storms and cascading failures across a dependency tree
- Schema drift and incompatible changes in contracts
- Cross region policy inconsistencies and auth outages

### Internal architecture notes
- Budget based retries and hedging with circuit breakers and bulkheads
- Contract first APIs with schema registry and backward compatibility
- SLO driven alert rules and error budget policies

### Validation and references
- Fault injection and chaos experiments across dependencies
- Conformance tests in CI with contract and policy validation
- Industry literature on resilience engineering and SRE practices

### Trade offs revisited
- Resilience overhead vs tail latency; observability cost vs debugging speed

### Implementation guidance
- Provide paved road libraries for retries, timeouts, breakers, auth, and telemetry
- Progressive delivery with canaries and feature flags; automatic rollback triggers
