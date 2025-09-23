# Exemplar Metrics - Production Notes

## Overview

Attach trace exemplars to metrics (latency, errors) so you can jump from spikes to specific traces in dashboards.

## Patterns

- Record metrics while a span is current so the SDK can attach exemplars
- Enable exemplar storage in Prometheus (remote_write exemplar config) and Grafana
- Use exemplars on key SLO metrics (request_duration_seconds, error_rate)

## Example (Micrometer + OTel)

```java
Timer timer = Timer.builder("http_server_duration")
    .publishPercentileHistogram()
    .register(meterRegistry);

try (var span = tracer.nextSpan().name("handle").start()) {
    long start = System.nanoTime();
    // ... handle request ...
    timer.record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
}
```

## Pitfalls

- Storage overhead; restrict to critical metrics
- Sampling: ensure traces are sampled for exemplar links to be useful

## Deep Dive Appendix

### Adversarial scenarios
- Missing or mismatched trace context in metrics
- Cardinality bloat from exemplar labels
- Exporter backpressure causing dropped exemplars

### Internal architecture notes
- Exemplars linking metric samples to trace ids
- Sampling strategy coordination between tracing and metrics
- Storage and query considerations for exemplars

### Validation and references
- Correlation tests from incident traces to metrics
- Load tests on metric ingest with exemplar density
- OpenTelemetry and Prometheus exemplar literature

### Trade offs revisited
- Debugging power vs storage and ingest cost

### Implementation guidance
- Enable exemplars for critical SLIs; cap labels; monitor drop rates
