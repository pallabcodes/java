# Circuit Breaker - Netflix Production Guide

## 🎯 Concept overview

Circuit breakers prevent cascading failures by failing fast when a dependency becomes unhealthy and probing recovery with controlled requests.

## 📊 Implementation layer classification

| Component | Layer | Type | Netflix status |
|---|---|---|---|
| Per dependency breaker | Application | client side policy | Production |
| Bulkhead isolation | Application | thread pool or queue isolation | Production |
| Adaptive breaker | Application | SRE tuned budgets and SLO aware | Production |

## 🚀 Production implementations

* State machine with closed, open, and half open states
* Failure and slow request rate tracked over rolling windows
* Separate isolation domain per dependency with thread or concurrency limits
* Timeouts and retry budgets integrate with breaker decisions

## 🧭 Production Readiness Addendum

### Techniques and where to use
* Failure rate threshold over rolling window with minimum request volume
* Slow call rate threshold based on P99 or concrete latency bound
* Half open with limited probe concurrency and jittered probe intervals
* Sliding window by time for bursty traffic and by count for steady traffic
* Per endpoint breakers for hot calls, global breaker for service wide shed
* Fallbacks: cached value, default response, degrade features, queue write

### Trade offs
* Latency: breaker adds minimal overhead but prevents long timeouts
* Availability: fail fast improves availability at the caller while reducing features
* Network: fewer retries under failure reduce amplification
* Process: tuning thresholds requires production data and SLO context
* OS: isolation pools avoid starvation but reduce peak throughput
* Cost: degraded mode may accept lower quality responses
* Complexity: interactions with retries, hedging, and rate limiting

### Quantified trade offs
* Overhead: per request breaker checks add 5 to 30 microseconds in process with ring buffer metrics; negligible versus network IO.
* Threshold tuning: set minimum request volume to at least 50 to 200 requests per window to reduce false trips at low traffic. Failure rate threshold 20 percent to 50 percent depending on SLO and fallback strength.
* Window size: time window 10 to 60 seconds captures recent conditions without being too noisy. Larger windows delay recovery by similar factor.
* Half open budget: probe concurrency 1 percent to 5 percent of steady state RPS per instance caps blast radius while accelerating recovery. Add 10 percent jitter.
* Isolation pools: limit concurrent downstream calls to P99_latency × target_RPS by Little law. Example: P99 200 ms and target 200 RPS gives 40 concurrent.

### Failure modes and mitigations
* Flapping between states when threshold near boundary: add hysteresis and extended open duration
* Cold start misclassification with low volume: require minimum number of calls before evaluation
* Hidden head of line blocking in shared pools: per dependency bulkheads
* Black hole timeouts dominate: set timeouts lower than breaker thresholds
* Thundering probes from many instances: randomize half open probes with jitter and budget

### Sizing and capacity
* Probe concurrency budget proportional to steady state RPS per instance
* Isolation pool sizes from latency and concurrency targets through Little law
* Choose rolling window size to include enough samples for statistical stability

### Verification
* Fault injection for latency and errors to verify open and recovery
* Replay traffic in staging to validate thresholds and false trip rates
* Chaos experiments on dependencies while watching error budget burn

### Production checklist
* Metrics: open rate, half open success rate, rejection count, slow call rate, fallback usage
* Alerts: sustained open breakers for key dependencies, fallback exhaustion, isolation pool saturation
* Runbooks: widen thresholds temporarily, increase isolation pool, disable hedging, move traffic

## 📊 Technique Trade offs Matrix (Internal)

| Technique | Latency | Reliability | Cost | Complexity | Notes |
|---|---|---|---|---|---|
| Failure rate breaker | low | high | low | low | good default
| Slow call breaker | low | high | low | low | protects tail latency
| Half open probes | low | high | low | medium | safe recovery
| Bulkheads | low | high | low | medium | prevent starvation
| Adaptive breaker | low | very high | medium | high | SLO aware budgets
