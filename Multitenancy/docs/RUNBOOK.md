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
- **Encryption**: Data encrypted at rest and in transit
- **Backup Security**: Encrypted backups
- **Audit Logging**: Comprehensive audit trails
- **PII Handling**: Proper handling of personally identifiable information

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
