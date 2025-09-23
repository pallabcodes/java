# API Gateway - Netflix Production Guide

## 🎯 Concept overview

API Gateway acts as the single entry point for clients, handling routing, auth, rate limiting, aggregation, and observability.

## 📊 Implementation layer classification

| Component | Layer | Type | Netflix status |
|---|---|---|---|
| Gateway Routing | Infrastructure | Reverse proxy routing | Production |
| Authn/Z | Application + Infrastructure | JWT/OAuth2, mTLS | Production |
| Policies | Application | Rate limiting, WAF | Production |

## 🚀 Production implementations

- Envoy/NGINX/Kong for routing, filters, plugins
- Spring Cloud Gateway for JVM-based composition
- Integration with OAuth2 providers, mTLS, SPIFFE

## 🧭 Production Readiness Addendum

### Techniques and where to use
- Route by path/host/headers; canary/blue-green via weights
- JWT validation, OAuth2 introspection, mTLS for service identity
- Rate limiting: token bucket at edge + per-tenant enforcement downstream
- Request/response transformation, aggregation for mobile clients

### Trade-offs
- Latency: extra hop; minimize heavy transformations
- Network: TLS termination CPU; prefer hardware acceleration where available
- Process: hot reload of config; distributed control plane for scale
- OS: file descriptors, kernel buffers; tune per workload
- Cost: logging/metrics volume; sampling for non-error traffic
- Complexity: aggregation and business logic at gateway can increase coupling

### Quantified trade offs
* Hop latency: gateway adds 0.5 to 2 ms p50 and 2 to 8 ms p99 for simple routing and auth checks within AZ. Heavy transforms can add 5 to 20 ms.
* TLS cost: budget 1 to 3 CPU cores per 10k RPS for TLS termination at 1 KB payload. Enable session resumption to cut handshake cost by 50 percent to 80 percent.
* Rate limiting: token bucket at edge introduces 0.1 to 0.3 ms overhead per request when in memory. External quota calls add 1 to 3 ms per check.
* JSON processing: parsing 1 KB payload costs 0.1 to 0.3 ms on JVM with Jackson; 10 KB adds 1 to 3 ms. Prefer streaming for large bodies.
* Logging and tracing: full body logging can add 5 to 20 ms and large IO. Use sampling and redact sensitive fields.

### Failure modes and mitigations
- Config errors: staged rollout, schema validation, dry-run
- Overload: autoscale, circuit break, backpressure to clients
- Auth outages: token cache with TTL, grace policies, fail-closed for sensitive routes

### Sizing and capacity
- Per-core RPS for TLS and JSON parsing; reserve ≥30% headroom
- Policy evaluation budgets; precompile regex, avoid synchronous lookups

### Verification
- Contract tests for routes and policies
- Fault injection for upstream errors/latency; ensure proper retries/timeouts

### Production checklist
- Metrics: RPS, P50/P99, 4xx/5xx by route, auth failures, RL rejections
- Alerts: 5xx surge by route, auth failure spikes, config push failures
- Runbooks: rollback policy, drain node, rotate certificates

## 📊 Technique Trade-offs Matrix (Internal)

| Technique | Latency | Reliability | Cost | Blast Radius | Complexity | Notes |
|---|---|---|---|---|---|---|
| L7 Gateway (Envoy/NGINX/Kong) | low | high | CPU for TLS | per tier | medium | strong plugin/filter ecosystem |
| JVM Gateway (SCG) | low-med | high | JVM overhead | per tier | medium | flexible composition |
| Mesh Ingress | low | high | control-plane | per cluster | high | native mesh integration |

## Deep Dive Appendix

### Adversarial scenarios
- Misconfigured policies blocking critical traffic
- JWT or OAuth provider outages impacting auth
- Heavy transformations causing tail latency regressions

### Internal architecture notes
- Filter and plugin model with policy ordering and budgets
- Cached token introspection and key rotation handling
- Aggregation adapters and streaming transforms

### Validation and references
- Contract tests for routes and policies per service
- Fault injection on upstreams and auth providers
- Literature on gateway patterns and security models

### Trade offs revisited
- Centralization efficiency vs coupling risk; transform power vs latency

### Implementation guidance
- Canary policy changes; default deny with explicit allows; robust fallbacks
- Rate limit at edge and enforce per tenant quotas downstream
