### Netflix production grade microservices toolchain

**Service discovery and registry**

1. Eureka, Consul, Zookeeper

**API gateway and edge**

1. Zuul or Zuul 2, Spring Cloud Gateway, NGINX at edge

**Client side load balancing and service communications**

1. Ribbon, Spring Cloud LoadBalancer, Feign, REST, gRPC

**Resilience and fault tolerance**

1. Hystrix, Resilience4j, retry and backoff, bulkheads, timeouts

**Dynamic configuration and feature flags**

1. Archaius, Spring Cloud Config, LaunchDarkly or Unleash

**Caching**

1. EVCache, Redis, Caffeine

**Messaging and streaming**

1. Kafka, SQS or SNS, Kinesis, Debezium CDC

**Data stores**

1. Cassandra, Postgres or MySQL, Elasticsearch, S3 or MinIO

**Observability**

1. Atlas, Prometheus, Micrometer, Zipkin or Jaeger, Grafana, centralized logging ELK or Loki

**Security and secrets**

1. OAuth2 or OIDC provider Okta or Auth0 or Keycloak, RBAC or ABAC, Vault, mTLS, OPA or Cedar

**CI CD and release**

1. Spinnaker, Jenkins or GitHub Actions, Docker, Kubernetes or Titus, Helm or Kustomize

**Service mesh and networking**

1. Envoy, Istio or Linkerd, rate limiting and quotas, eBPF for networking

**Schema and API contracts**

1. OpenAPI, AsyncAPI, protobuf for gRPC, consumer driven contracts Pact

**Chaos, testing, and SRE**

1. Chaos Monkey, FIT, Gatling or k6, synthetic checks, SLOs with Alertmanager

**Workflow and orchestration**

1. Netflix Conductor, Temporal or Cadence, Airflow for batch, Genie

**Data processing and analytics**

1. Spark, Flink, Presto or Trino, Hive or Iceberg


### Status in `Multitenancy`

**Implemented**

1. Eureka client and discovery annotations
2. Ribbon and Feign for client calls, load balanced `RestTemplate`
3. Hystrix based circuit breakers and bulkhead style thread pools
4. Archaius based dynamic configuration
5. Micrometer with Prometheus, Zipkin via Sleuth, correlation ID, request metrics
6. Static analysis and dependency checks SpotBugs, Checkstyle, PMD, OWASP Dependency Check
7. Caching with Caffeine, Redis integration present, cache names and config
8. Postgres with performance indexes and Hikari tuning
9. S3 compatible storage via MinIO, local storage switch
10. OpenAPI with bearer auth and standardized error schema
11. Rate limiting with Bucket4j and per tenant dynamic config
12. Webhooks with HMAC, retries, exponential backoff, admin controls
13. Workflow engine, comments, labels, watchers, SLA scheduler
14. Dockerfile, docker compose, MinIO compose
15. Runbook, security, performance docs

**Partially implemented**

1. Redis used for config overrides not EVCache style multi region cache
2. Distributed tracing wired to Zipkin, Atlas not used
3. Reporting implemented but no external OLAP or Presto
4. Eventing is internal Spring events, no Kafka

**Not yet implemented**

1. API gateway at edge Zuul or Spring Cloud Gateway with global auth, rate limit, header policies
2. Service mesh Envoy or Istio, mTLS, traffic shaping, canary and fault injection
3. CI CD with Spinnaker pipelines, deployment strategies red black or canary
4. Chaos engineering Chaos Monkey or FIT integration
5. Secrets management Vault, envelope encryption and dynamic DB creds
6. OAuth2 or OIDC provider integration Okta or Keycloak, token introspection for services
7. EVCache for low latency multi region caching
8. Kafka based event bus, outbox pattern, CDC with Debezium
9. gRPC for internal service to service calls with protobuf
10. Centralized logging stack ELK or Loki and log shipping
11. Consumer driven contracts Pact and contract verification in CI
12. Conductor for long running workflows or Temporal based orchestration
13. Feature flag service LaunchDarkly or Unleash and gradual rollout
14. Multi region active active patterns, region failover runbooks and data replication
15. Atlas metrics backend and Netflix style dimensional dashboards
16. Data search stack Elasticsearch and Kibana for issues search and analytics
17. Policy layer OPA for fine grained authorization decisions
18. Secrets rotation and KMS integration
19. Canary analysis Kayenta or equivalent automated canary judge
20. Kubernetes or Titus production manifests, autoscaling, PodDisruptionBudgets, HPA and VPA
21. Cost and quota guardrails service level budgets and alerts

This file captures the reference toolchain and current adoption state without implementation steps.


