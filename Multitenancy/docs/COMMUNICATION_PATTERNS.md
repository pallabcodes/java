# Inter service communication patterns

## Synchronous patterns

- REST over HTTP via Feign clients
  - Implemented: yes
  - Why: simple contracts, observability friendly, resilient via retries and circuit breakers
  - How: Feign with Eureka discovery and client-side load balancing, Resilience4j timeouts retries bulkheads and circuit breakers, JWT and tenant headers propagation

- API Gateway routing
  - Implemented: yes
  - Why: central ingress, tenant-aware rate limiting, secure headers, CORS
  - How: Spring Cloud Gateway with key resolver and rate limiting

- gRPC unary or streaming
  - Implemented: no
  - Why not: current services do not need binary RPC or bidirectional streaming; REST covers needs while minimizing operational surface

- GraphQL
  - Implemented: no
  - Why not: aggregation needs limited; REST endpoints are stable and cacheable; deferring the extra gateway layer

- Hedged requests (tail latency mitigation)
  - Implemented: yes
  - Why: enabled Feign client hedging for core service to reduce tail latency

- Idempotency keys for sync writes
  - Implemented: yes
  - Why: enforced via servlet filter with Redis backed key claim per tenant

## Asynchronous patterns

- Kafka pub/sub with Avro and Schema Registry
  - Implemented: yes
  - Why: high throughput, durability, ordering per key, mature tooling
  - How: transactional outbox to Kafka, Avro schemas with backward compatibility, idempotency store, DLQ monitoring

- Transactional outbox
  - Implemented: yes
  - Why: atomic write of state and events; avoids dual-write risk

- Debezium CDC
  - Implemented: scaffolded
  - Why: optional alternative to outbox for change capture; kept as a future option

- SQS or SNS
  - Implemented: no
  - Why not: standardized on Kafka for on-prem and local; avoid multi-broker complexity

- Redis Streams or NATS
  - Implemented: no
  - Why not: Kafka already provides required durability and scale

- Async request–reply over Kafka (correlationId)
  - Implemented: no
  - Why not: current flows are fire-and-forget; add when RPC-like async patterns are required

- Stream processing (Kafka Streams/ksqlDB/Flink)
  - Implemented: no
  - Why not: reporting handled via services and DB; add when continuous aggregations are needed at stream layer

## Intra-process patterns

- Spring application events
  - Implemented: yes
  - Why: local decoupling for domain signals without cross-service coupling

## Mesh and identity

- Istio mTLS and traffic policy
  - Implemented: yes
  - Why: zero-trust in cluster; secure service-to-service; enables canary and locality failover

- SPIFFE identities
  - Implemented: yes
  - Why: strong workload identity; future-friendly for policy decisions

## Contract and policy

- Pact provider verification
  - Implemented: yes
  - Why: guards synchronous REST contracts in CI

- OPA policies with tests
  - Implemented: yes
  - Why: centralized authorization rules with CI validation

- Consumer-side Pact tests in CI
  - Implemented: no
  - Why not: provider gate covers our current surface; add consumer tests when external consumers formalize contracts

## Rationale summary

- Latency and traffic profile suits REST plus resilient clients for synchronous needs
- Event workflows benefit from Kafka throughput and outbox guarantees
- Simplicity and operator load guided deferral of gRPC, GraphQL, and alternate brokers
- Additional deferrals (hedging, async request–reply, stream processing, consumer pacts) are ready to adopt when corresponding product and latency needs arise
