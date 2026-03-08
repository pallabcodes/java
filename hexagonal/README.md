# Hexagonal Microservice Template

Baseline Spring Boot 3 + Java 21 template using DDD + Hexagonal (Ports & Adapters). This repo is meant to be cloned and tuned per service.

**Structure**
- `domain` domain model and events
- `application` use cases and ports
- `adapter/in` REST, gRPC, GraphQL
- `adapter/out` persistence, messaging, cache, storage
- `platform` configuration and cross-cutting concerns

**Feature Toggles (application.yml)**
- `app.persistence.mode`: `jpa` (default), `mongo`, `dynamo`
- `app.messaging.mode`: `kafka`, `rabbit`, `sns`
- `app.cache.enabled`: `true|false`
- `app.storage.mode`: `s3`
- `app.outbox.polling-enabled`: `true|false`

**Local Dev**
- Start dependencies: `docker compose -f ops/compose/docker-compose.dev.yml up`
- Run app: `./gradlew bootRun`
- REST: `POST /samples`, `GET /samples/{id}`
- GraphQL: `/graphql` (GraphiQL enabled)
- gRPC: see `src/main/proto/sample.proto`

**Observability**
- OTLP exporter via environment variables such as `OTEL_EXPORTER_OTLP_ENDPOINT`
- Prometheus scrapes `/actuator/prometheus`
- Loki/Grafana are wired via the OTEL collector in `ops/otel`

**Notes**
- Update `group` and base package (`com.yourorg.platform`) to your org
- Dependency versions are pinned in `gradle.properties`
