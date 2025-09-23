# WebSockets - Netflix Production Guide

## 🎯 Concept overview

WebSockets provide full duplex persistent connections between clients and servers for low latency push messaging.

## 📊 Implementation layer classification

| Component | Layer | Type | Netflix status |
|---|---|---|---|
| Gateway termination | Infrastructure | WS to HTTP bridge | Production |
| Stateful WS service | Application | session fanout | Production |
| Broker backed WS | Application + Infrastructure | pub sub | Production |

## 🚀 Production implementations

- Terminate WS at edge and forward to WS fleets with sticky routing
- Use brokers for fanout and presence state
- Backpressure and rate limits per connection

## 🧭 Production Readiness Addendum

### Techniques and where to use
- Use WS for realtime updates, presence, chat, live controls
- Use fallback SSE or polling for constrained clients
- Heartbeats and ping pong for liveness

### Trade offs
- Latency: persistent connections minimize setup overhead
- Network: many connections; tune keepalives and TCP settings
- Process: rolling restarts drain connections gracefully
- OS: file descriptor limits, epoll/kqueue tuning
- Cost: long lived connections consume resources
- Complexity: backpressure, reconnect logic, ordering guarantees

### Quantified trade offs
* Connection scale: per connection memory 10 to 50 KB depending on stack; 1 million connections needs 10 to 50 GB across the fleet.
* Heartbeats: 30 s ping with 3 fails detection yields ~90 s detection; 15 s with 2 fails yields ~30 s with 2x QPS.
* Backpressure: cap per connection outbound queue at 100 to 1000 messages; drop or coalesce beyond to protect memory.
* Fanout: broker mediated fanout scales to millions of subscribers; direct fanout from service limited by egress and CPU.

### Failure modes and mitigations
- Thundering reconnects: jittered backoff, exponential backoff
- Head of line blocking: per connection queues and drop policies
- Stale connections: server side idle timeouts and heartbeats

### Sizing and capacity
- Plan max concurrent connections per node, CPU and memory per connection
- Broker throughput for fanout patterns

### Verification
- Load tests with millions of connections and churn
- Chaos: drop network segments, kill nodes, verify reconnect budgets

### Production checklist
- Metrics: connections, churn, message rate, backpressure drops, latency
- Alerts: error spikes, reconnect storms, CPU or FD saturation
- Runbooks: rotate nodes, scale fleet, broker partition handling

## 📊 Technique Trade offs Matrix (Internal)

| Technique | Latency | Scale | Cost | Complexity | Notes |
|---|---|---|---|---|---|
| Direct WS | very low | medium | medium | medium | sticky routing |
| WS + Broker | low | high | medium | high | fanout and presence |
| SSE fallback | low | medium | low | low | server push only |

## Deep Dive Appendix

### Adversarial scenarios
- Reconnect storms on regional failure or deploy
- Slow consumer buildup leading to memory pressure
- Head of line blocking with per connection ordering

### Internal architecture notes
- Stateful connection routing with sticky session or consistent hashing
- Backpressure with bounded queues and drop policies per channel
- Presence and fanout offloaded to broker for scale

### Validation and references
- Million connection load tests with churn and heartbeats
- Chaos experiments: kill nodes, drop network, throttle broker partitions
- Papers on scalable pub sub and congestion control

### Trade offs revisited
- Latency vs consistency of ordering; scale vs cost of connection state

### Implementation guidance
- Jittered reconnect backoff and budgets per client to avoid storms
- Quotas per tenant and per connection; autoscale and pre warm fleets
