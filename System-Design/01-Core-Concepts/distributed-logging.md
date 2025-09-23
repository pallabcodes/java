# Distributed Logging - Netflix Production Guide

## Concept overview

Distributed logging collects structured events from many services and correlates them for request level visibility. Netflix style logging includes correlation identifiers, structured fields, sampling, privacy controls, and shipping to a central store for search and analysis.

## Implementation layer classification

| Component | Layer | Type | Netflix status |
|---|---|---|---|
| Correlation id propagation | Application | Context and headers | Production |
| Structured logging | Application | JSON fields | Production |
| Log shipping | Application plus infrastructure | Agent or http exporter | Production |
| Privacy controls | Application | PII scrubbing | Production |
| Retention and search | Infrastructure | Elasticsearch, Loki | Production |

## Patterns

- Correlate by request using an id that is present in logs, metrics, and traces
- Use structured fields for service, version, tenant, user, and operation
- Sample noisy categories while always logging error paths
- Redact private data at the edge

## Production usage

```java
// create or extract correlation id
String cid = CorrelationContext.startOrResume(request.getHeader("X-Correlation-Id"));

// log with structured fields
StructuredLogger logger = StructuredLogger.get(YourClass.class);
logger.info("movie_fetch", Map.of(
    "correlation_id", cid,
    "service", "catalog",
    "movie_id", movieId,
    "success", true
));
```

## Troubleshooting

- Missing id: ensure the filter runs first and always sets the id
- High volume: sample verbose categories but keep errors and warnings
- Sensitive data: apply a scrubber function before logging

## References

- Netflix tech blog logging and observability articles
- OpenTelemetry semantic conventions for logs

## Deep Dive Appendix

### Adversarial scenarios
- Correlation loss across async boundaries
- Log loss under backpressure and exporter outages
- PII leakage risks in structured logs

### Internal architecture notes
- Trace and span propagation via headers and MDC
- Structured logging with sampling and redaction policies
- Exporters with retry, backoff, and bounded queues

### Validation and references
- End to end trace to log correlation tests
- Load and backpressure tests on exporters
- Literature on observability and structured logging best practices

### Trade offs revisited
- Verbosity vs cost; correlation depth vs overhead

### Implementation guidance
- Enforce correlation middleware; redact by default; sample non error paths
- Dedicated log pipelines with quotas and SLOs
