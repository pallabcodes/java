# Proxy vs Reverse Proxy - Netflix Production Guide

## 🎯 Concept overview

- Proxy: client-side egress intermediary that forwards client requests to upstream servers (privacy, policy, caching, auth).
- Reverse Proxy: server-side ingress intermediary that fronts services (routing, TLS termination, authn/z, rate limiting, WAF, load balancing).

## 📊 Implementation layer classification

| Component | Layer | Type | Netflix status |
|---|---|---|---|
| Forward Proxy | Infrastructure | Egress control, policy | Production |
| Reverse Proxy | Infrastructure | Ingress control, routing | Production |
| Sidecar Proxy | Application + Infrastructure | Per-pod proxy (mesh) | Production |

## 🚀 Production implementations

- NGINX/Envoy as reverse proxy and API gateway
- Envoy forward proxy for egress control, domain allowlists
- Service mesh (Istio/Envoy) for mTLS, retries, timeouts, traffic shifting

## 🧭 Production Readiness Addendum

### Techniques and where to use
- Reverse proxy for TLS termination, routing, canary, A/B, WAF
- Forward proxy for egress policy, DNS pinning, data exfil protection
- Sidecar for per-service mTLS, retries/timeouts, circuit breaking

### Trade-offs
- Latency: extra hop; keep within same AZ/host where possible
- Network: head-of-line on shared proxies; scale horizontally, shard by tenant
- Process: config reloads must be hitless; use hot reload (Envoy XDS)
- OS resources: file descriptors, kernel buffers; tune ulimits and TCP
- Cost: TLS offload CPU, logging volume; sample non-error logs
- Complexity: meshes add control plane dependencies

### Quantified trade offs
* Latency per hop: intra AZ reverse proxy adds 0.2 to 0.8 ms p50 and 1 to 3 ms p99 when on the same host or rack. Cross AZ adds 0.5 to 2 ms p50.
* TLS termination: RSA or ECDSA handshakes add 5 to 20 ms on cold connection and 0.1 to 0.4 ms on resumed session. Budget 1 to 3 CPU cores per 10k RPS at 1 KB payload with TLS on modern ciphers.
* Throughput: single proxy instance saturates at 5 to 20 Gbps depending on NIC offload and kernel tuning. Plan 30 percent headroom.
* File descriptors: one connection consumes one descriptor. Keep per instance limit above 100k for large fan in. Monitor SYN backlog and accept queues.
* Logging: full request logging adds 1 to 3 ms per request and high storage cost. Prefer 1 to 5 percent sampling for non error traffic.
* Config churn: dynamic config pushes of 100 routes per second can spike CPU by 5 to 10 percent on control plane and a few percent on data plane. Stagger pushes.

### Failure modes and mitigations
- Config push error: staged rollout, canary validation, automatic rollback
- Proxy overload: autoscale, rate limit, circuit break upstreams
- TLS issues: enable OCSP stapling, consistent cipher suites, cert rotation runbook

### Sizing and capacity
- Compute per-core RPS for TLS and non-TLS; reserve headroom (≥30%)
- Buffer sizes: align with payloads; avoid excessive memory per connection

### Verification
- Conformance tests for routing, headers, auth, timeouts, retries
- Fault injection (abort/delay) to validate resilience policies

### Production checklist
- Metrics: RPS, P50/P99 latency, 4xx/5xx, upstream errors, open connections
- Alerts: surge in 5xx, upstream connect errors, config push failures
- Runbooks: rollback config, drain node, cert rotation, hot reload

## 📊 Technique Trade-offs Matrix (Internal)

| Technique | Latency | Reliability | Cost | Blast Radius | Complexity | Notes |
|---|---|---|---|---|---|---|
| Reverse Proxy (Envoy/NGINX) | low | high with HA | CPU for TLS | per tier | medium | use hitless reload, health checks |
| Forward Proxy | low | medium | modest | per cluster | medium | egress policy, caching optional |
| Service Mesh Sidecar | low-med | high with mTLS | control-plane | per pod | high | powerful but added ops overhead |

## Deep Dive Appendix

### Adversarial scenarios
- Config push with bad routes causing blackholes
- TLS cert expiry or mismatch across tiers
- Egress policy gaps allowing data exfiltration

### Internal architecture notes
- Control plane driven dynamic config with staged rollout and validation
- mTLS with SPIFFE identities and per route authz policies
- Outlier detection and circuit breaking at proxy layer

### Validation and references
- Conformance suites for routing and policy; synthetic probes
- Fault injection: abort and delay filters to test resilience behavior
- Literature on service mesh control planes and data planes

### Trade offs revisited
- Policy centralization vs service autonomy; hop cost vs capabilities

### Implementation guidance
- Canary config changes; enforce schema and policy checks
- Certificate rotation playbooks and automated renewal
