# Production Runbook

## Overview
This document provides operational procedures for the Netflix Productivity Platform in production environments.

## System Architecture

### Components
- **API Gateway**: Spring Boot application with multi-tenant support
- **Database**: PostgreSQL with tenant isolation
- **Cache**: Redis for session management and caching
- **Storage**: MinIO for file attachments
- **Monitoring**: Micrometer + Prometheus metrics

### Key Services
- Authentication & Authorization
- Issue Management
- Project Management
- Workflow Engine
- Reporting & Analytics
- File Attachments
- Webhooks
- SLA Management

## Deployment

### Prerequisites
- Java 17+
- PostgreSQL 13+
- Redis 6+
- MinIO (or S3-compatible storage)

### Environment Variables
```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/productivity
SPRING_DATASOURCE_USERNAME=productivity_user
SPRING_DATASOURCE_PASSWORD=secure_password

# Redis
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=redis_password

# MinIO
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# Multi-tenancy
MULTITENANCY_ENABLED=true
```

### Health Checks
- **Application**: `GET /actuator/health`
- **Database**: `GET /actuator/health/db`
- **Redis**: `GET /actuator/health/redis`
- **Storage**: `GET /actuator/health/storage`

## Monitoring

### Key Metrics
- Request rate and latency
- Database connection pool usage
- Cache hit/miss ratios
- SLA breach rates
- File upload/download rates
- Webhook delivery success rates

### Alerts
- High error rate (>5%)
- High latency (>2s p95)
- Database connection pool exhaustion
- SLA breach rate >10%
- Webhook delivery failures >20%

### Dashboards
- **Application Performance**: Request rates, response times, error rates
- **Database Performance**: Query performance, connection pool, slow queries
- **Business Metrics**: Issue throughput, SLA compliance, team productivity

## Troubleshooting

### Common Issues

#### High Memory Usage
1. Check for memory leaks in heap dumps
2. Review cache configuration and eviction policies
3. Monitor JVM garbage collection logs
4. Scale horizontally if needed

#### Database Performance
1. Check slow query logs
2. Review database connection pool metrics
3. Analyze query execution plans
4. Consider read replicas for reporting queries

#### SLA Breaches
1. Check SLA configuration and rules
2. Review issue assignment patterns
3. Analyze team capacity vs workload
4. Adjust SLA thresholds if needed

#### Webhook Failures
1. Check webhook endpoint availability
2. Review retry policies and backoff
3. Monitor webhook delivery logs
4. Verify authentication credentials

### Log Analysis
```bash
# Application logs
kubectl logs -f deployment/productivity-api

# Database logs
kubectl logs -f deployment/postgres

# Redis logs
kubectl logs -f deployment/redis
```

## Maintenance

### Database Maintenance
- **Backup**: Daily automated backups
- **Vacuum**: Weekly VACUUM ANALYZE
- **Index Maintenance**: Monthly index optimization
- **Schema Migrations**: Zero-downtime deployments

### Cache Maintenance
- **Redis Memory**: Monitor memory usage and eviction
- **Cache Warming**: Pre-populate frequently accessed data
- **Cache Invalidation**: Implement proper cache invalidation strategies

### Storage Maintenance
- **File Cleanup**: Remove orphaned files
- **Storage Monitoring**: Monitor disk usage and performance
- **Backup**: Regular backup of critical files

## Scaling

### Horizontal Scaling
- **API Servers**: Scale based on CPU and memory usage
- **Database**: Read replicas for reporting queries
- **Cache**: Redis cluster for high availability
- **Storage**: MinIO cluster for file storage

### Vertical Scaling
- **API Servers**: Increase memory and CPU
- **Database**: Increase memory for better caching
- **Cache**: Increase Redis memory
- **Storage**: Increase storage capacity

## Security

### Access Control
- **Authentication**: JWT tokens with proper expiration
- **Authorization**: Role-based access control (RBAC)
- **Multi-tenancy**: Tenant isolation at database level
- **API Security**: Rate limiting and input validation

### Data Protection
### Key and Token Lifecycle
- JWT signing keys
  - Rotation: maintain two active keys, rotate every 30 days
  - Store in Vault or KMS, expose via JWKS if external IdP
  - Rollback: keep previous key active for 48 hours
  - Break glass: disable rotation and pin to previous key in outage
- Refresh tokens
  - Rotation on each use, revoke lineage on reuse detection
  - Store device fingerprint and client metadata
- Secrets
  - Vault KV for app secrets, DB dynamic creds via database plugin
  - Rotate DB creds quarterly or on incident
- mTLS
  - Issue client certs via PKI, rotate quarterly
  - Enforce client auth in mesh or app as needed
  - SPIFFE identities where available in k8s

- **Encryption**: Data encrypted at rest and in transit
- **Backup Security**: Encrypted backups
- **Audit Logging**: Comprehensive audit trails
- **PII Handling**: Proper handling of personally identifiable information

### OIDC Dev Setup

- Run a local Keycloak on port 8089 or use a hosted dev tenant.
- Create realm `productivity`, client `productivity-api` with audience `api`.
- Configure `application-oidc.yml` and start with `SPRING_PROFILES_ACTIVE=oidc`.
- Verify JWT validation: call `/api/issues` with `Authorization: Bearer <token>`.

## Disaster Recovery

### Backup Strategy
- **Database**: Daily full backups + WAL archiving
- **Files**: Regular backup of MinIO storage
- **Configuration**: Version-controlled configuration
- **Code**: Git repository with proper branching

### Recovery Procedures
1. **Database Recovery**: Restore from latest backup
2. **File Recovery**: Restore from MinIO backup
3. **Application Recovery**: Redeploy from Git
4. **Configuration Recovery**: Apply configuration from version control

### RTO/RPO Targets
- **RTO**: 4 hours (Recovery Time Objective)
- **RPO**: 1 hour (Recovery Point Objective)

## Performance Tuning

### JVM Tuning
```bash
# Heap size
-Xms2g -Xmx4g

# Garbage collection
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200

# Memory settings
-XX:MetaspaceSize=256m
-XX:MaxMetaspaceSize=512m
```

### Database Tuning
```sql
-- Connection pool
max_connections = 200
shared_buffers = 256MB
effective_cache_size = 1GB

-- Query optimization
random_page_cost = 1.1
effective_io_concurrency = 200
```

### Application Tuning
- **Connection Pool**: Optimize database connection pool size
- **Cache Settings**: Tune cache TTL and eviction policies
- **Thread Pool**: Optimize thread pool sizes
- **Batch Processing**: Use batch operations for bulk data

## Incident Response

### Severity Levels
- **P1**: System down, data loss, security breach
- **P2**: Major functionality impacted
- **P3**: Minor functionality impacted
- **P4**: Cosmetic issues

### Response Times
- **P1**: 15 minutes
- **P2**: 1 hour
- **P3**: 4 hours
- **P4**: 24 hours

### Escalation
1. **On-call Engineer**: Initial response
2. **Team Lead**: P1/P2 incidents
3. **Engineering Manager**: P1 incidents
4. **Director**: P1 incidents with business impact

## Contact Information

### On-call Rotation
- **Primary**: [Primary Engineer Contact]
- **Secondary**: [Secondary Engineer Contact]
- **Manager**: [Engineering Manager Contact]

### Escalation Contacts
- **Infrastructure**: [Infrastructure Team Contact]
- **Database**: [Database Team Contact]
- **Security**: [Security Team Contact]

## Gateway profile

Use the reactive gateway to front the API with secure headers and correlation id.

- Prereq: Core API runs at `8080`.
- Start API: `SPRING_PROFILES_ACTIVE=netflix ./mvnw spring-boot:run`
- Start Gateway: `SPRING_PROFILES_ACTIVE=gateway ./mvnw spring-boot:run`

Gateway listens on `8081` and forwards `/api/**` to `http://localhost:8080`.

Headers added

- `X-Correlation-Id` generated if absent
- `X-Frame-Options: DENY`
- `X-Content-Type-Options: nosniff`
- `Referrer-Policy: no-referrer`
- `Cache-Control: no-store`

Troubleshooting

- Run gateway in a separate process from the API.
- Gateway uses WebFlux; avoid activating servlet-only profiles together.

### Actuator endpoints

Gateway exposes actuator endpoints when the `gateway` profile is active.

- Health: `GET http://localhost:8081/actuator/health`
- Metrics: `GET http://localhost:8081/actuator/metrics`
- Prometheus: `GET http://localhost:8081/actuator/prometheus`

## Kafka and Outbox

### Local Kafka

Start local Kafka for outbox dispatching using the provided compose file.

```bash
docker compose -f docker-compose.kafka.yml up -d
```

### Application configuration

Ensure broker points to localhost in `application-netflix.yml`:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

### Verifying outbox dispatch

- Create an action that generates an audit event (for example, create a comment).
- Observe `outbox_events` table decrement and Kafka topic `audit-events` receive messages.

## Service Mesh

### Istio manifests

Apply base manifests to enable mTLS and route `/api/**`.

```bash
kubectl apply -f k8s/istio/productivity-namespace.yaml
kubectl apply -f k8s/istio/peerauthentication.yaml
kubectl apply -f k8s/istio/destinationrule-api.yaml
kubectl apply -f k8s/istio/authorizationpolicy.yaml
kubectl apply -f k8s/istio/virtualservice-gateway.yaml
```

Ensure the workload deployment in namespace `productivity` is labeled `app=productivity-api` and listens on port 8080.

To verify, port-forward the ingress gateway and hit `/api/issues`.

## OIDC with Keycloak

### Local Keycloak setup

Start Keycloak with PostgreSQL for OIDC authentication.

```bash
docker compose -f docker-compose.keycloak.yml up -d
```

### Keycloak configuration

1. Access Keycloak admin console at `http://localhost:8080`
2. Login with `admin/admin`
3. Create realm `productivity`
4. Create client `productivity-api` with client secret
5. Configure JWT settings in `application-keycloak.yml`
6. Import realm file `keycloak/realm-export.json` (Realm Settings -> Import)

### Testing OIDC flow

```bash
# Start application with Keycloak profile (default dev)
SPRING_PROFILES_ACTIVE=keycloak ./mvnw spring-boot:run

# Test token endpoint
curl -X POST http://localhost:8080/realms/productivity/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=productivity-api&username=user&password=password"
```

**Note**: Keycloak is the default development OIDC provider. For production, use `application-prod-oidc.yml` with your production IdP.

## Monitoring and Alerting

### Alert rules

Prometheus alert rules are defined in `monitoring/alerts.yml`:

- High error rate (>2%)
- High latency (p95 > 1.5s)
- Database connection pool exhaustion
- Outbox processing lag
- SLA breach rate (>10%)
- Webhook delivery failures (>20%)
- JVM heap usage (>80%)
- Circuit breaker open

### Backup procedures

#### PostgreSQL backup
```bash
./scripts/backup-postgres.sh [database_name] [backup_dir]
```

#### MinIO backup
```bash
./scripts/backup-minio.sh [bucket_name] [backup_dir]
```

Both scripts include automatic cleanup of backups older than 7 days.

## Canary traffic with Istio

### Subsets and weights

We define two subsets `v1` and `v2` in the `DestinationRule` and split traffic `90/10` in the `VirtualService`.

To roll forward, adjust weights and apply manifests:

```bash
kubectl apply -f k8s/istio/destinationrule-api.yaml
kubectl apply -f k8s/istio/virtualservice-gateway.yaml
```

Label your Deployments accordingly:

```bash
kubectl -n productivity set labels deploy/productivity-api-v1 app=productivity-api version=v1 --overwrite
kubectl -n productivity set labels deploy/productivity-api-v2 app=productivity-api version=v2 --overwrite
```

## Contract testing with Pact

Place consumer pacts in `src/test/resources/pacts` or project root `pacts/` and run provider verification:

```bash
./mvnw -Dtest=com.netflix.productivity.pact.ProviderPactTest test
```

## Environment readiness checklist

Run these quick checks before feature testing:

- Postgres reachable and migrations applied: `GET /actuator/health/db`
- Redis reachable: `GET /actuator/health/redis`
- MinIO reachable: `GET /api/storage/health`
- Kafka running (if outbox enabled): `docker compose -f docker-compose.kafka.yml ps`
- OIDC issuing tokens: `curl /realms/productivity/.well-known/openid-configuration`
- Gateway forwarding and rate limiting: `GET http://localhost:8081/api/issues` with `X-Tenant-ID`
