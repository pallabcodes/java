# Domain Name System (DNS) - Netflix Production Guide

## 🎯 Concept overview

DNS resolves human-readable names to IPs and provides naming, discovery, and traffic steering across regions and providers.

## 📊 Implementation layer classification

| Component | Layer | Type | Netflix status |
|---|---|---|---|
| Authoritative DNS | Infrastructure | Zone hosting, records | Production |
| Recursive DNS | Infrastructure | Caching resolver | Production |
| Service Discovery via DNS | Application + Infrastructure | SRV/NR records | Production |

## 🚀 Production implementations

- Managed authoritative DNS (Route53/Cloud DNS) with weighted/latency/geolocation policies
- Recursive resolvers with local caching and DNS over TLS
- Service discovery using DNS records or integration with mesh/gateway

## 🧭 Production Readiness Addendum

### Techniques and where to use
- Weighted/latency/geolocation routing for multi-region traffic steering
- Health-checked DNS failover for regional outages
- Split-horizon DNS for internal vs external views

### Trade-offs
- Latency: resolver cache reduces latency; TTL tuning affects freshness
- Network: packet loss and EDNS fragmentation; keep responses small
- Process: propagation delays; staged updates and gradual TTL reductions
- OS resources: resolver cache sizing and negative caching behavior
- Cost: query volume on managed DNS; cache at recursive resolvers
- Complexity: multiple providers add resiliency but increase ops burden

### Quantified trade offs
* TTL tuning: high TTL 300 to 600 seconds yields resolver hit ratios above 95 percent and reduces auth QPS by 3x to 10x, but delays failover by TTL. Low TTL 5 to 30 seconds improves steering but increases auth QPS 5x to 20x.
* Query latency: recursive cache hit 1 to 5 ms, miss to authoritative 15 to 60 ms intra region, cross region 60 to 150 ms.
* Packet size: keep under 1232 bytes for QUIC and common MTUs to avoid fragmentation. Use compressed records and minimal sets.
* Negative caching: set SOA MINIMUM and NXDOMAIN TTL to 30 to 60 seconds to reduce repeated misses while limiting staleness.
* Multi provider: dual authoritative providers increases monthly cost by 1.5x to 2x but reduces provider outage blast radius by half. Keep zones in sync via automation.

### Failure modes and mitigations
- Stale cache: use layered TTL policy, negative TTL controls
- Resolver outage: fall back resolvers, anycast resolver fleet
- Record bloat: compress, minimize records; prefer CNAME chains carefully

### Sizing and capacity
- Estimate QPS per client population; deploy local recursive caches
- TTL strategy: high TTL for static, lower TTL for failover records

### Verification
- DNS conformance tests for resolvers and auth zones
- Drill/dig automated checks across regions/providers

### Production checklist
- Metrics: QPS, NXDOMAIN rate, SERVFAIL, latency, cache hit ratio
- Alerts: SERVFAIL spikes, high NXDOMAIN, failover records activated
- Runbooks: record rollback, TTL reduction procedure, provider failover

## 📊 Technique Trade-offs Matrix (Internal)

| Technique | Latency | Freshness | Cost | Blast Radius | Complexity | Notes |
|---|---|---|---|---|---|---|
| High TTL records | lowest | low | lowest | large | low | great for static targets |
| Low TTL records | low | high | higher | per-record | medium | enables fast failover |
| Weighted routing | low | medium | medium | per region | medium | canary and traffic shifting |
| Latency-based | low | medium | medium | per client | medium | depends on resolver topology |
| Geo routing | low | medium | medium | per geography | medium | beware geolocation inaccuracies |

## Deep Dive Appendix

### Adversarial scenarios
- Resolver cache poisoning and stale entries
- EDNS fragmentation and packet loss across networks
- Split horizon inconsistencies between internal and external views

### Internal architecture notes
- Authoritative DNS with health checked records and weighted policies
- Recursive caches close to clients with DNS over TLS
- TTL strategies per record type and failover scenarios

### Validation and references
- Automated dig drills from multiple regions and providers
- Synthetic failover tests and propagation measurements
- Literature on DNS reliability, poisoning, and caching behaviors

### Trade offs revisited
- Freshness vs query load; record size vs fragmentation risk

### Implementation guidance
- Maintain dual providers; scripted rollbacks; TTL reduction plans
- Keep records minimal; prefer CNAME chains only when necessary
