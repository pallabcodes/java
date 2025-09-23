## Log Aggregation

### Objectives
* Centralize logs for search and correlation
* Power incident response with fast queries

### Pipeline
* App logs to stdout with structured JSON
* Sidecar or agent ships to collector
* Broker to storage and index

### Practices
* Consistent fields service env version request id tenant id
* Retention and tiering policy
* Privacy redaction and PII handling

### Reference pipeline
* Emit JSON logs with a consistent schema
* Ship via OpenTelemetry collector or Fluent Bit
* Store in Elasticsearch or Loki with lifecycle management

#### Java logging schema
```java
public final class LogContext {
    public static void put(String key, String value) { MDC.put(key, value); }
    public static void clear() { MDC.clear(); }
}
```

```java
// logback encoder example fields: timestamp, level, service, env, version, traceId, spanId, requestId, tenantId, message
```

#### Ingestion config example
```yaml
receivers:
  otlp:
    protocols:
      http:
      grpc:
exporters:
  loki:
    endpoint: http://loki:3100/loki/api/v1/push
processors:
  batch: {}
service:
  pipelines:
    logs:
      receivers: [otlp]
      processors: [batch]
      exporters: [loki]
```

### Review checklist
* Schema enforced and documented
* PII redaction tested and verified
* Retention meets compliance and cost targets
* Query examples for common incidents maintained


