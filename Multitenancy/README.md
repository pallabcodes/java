# Netflix Productivity Platform

## 🎯 **OVERVIEW**

A Netflix production-grade multi-tenant productivity platform (JIRA-like) designed for internal teams with focus on development speed while maintaining enterprise-grade quality and scalability.

## 🏗️ **ARCHITECTURE DECISION: MODULAR MONOLITH**

**Why Modular Monolith over Microservices:**
- **Development Speed**: Faster development and deployment
- **Team Size**: Internal tool doesn't need microservices complexity
- **Multi-tenancy**: Easier to implement with shared database
- **Maintenance**: Simpler operations and debugging
- **Future Scalability**: Can extract modules to microservices later

## 🚀 **KEY FEATURES**

### **Multi-Tenancy Support**
- **Tenant Isolation**: Complete data isolation per tenant
- **Dynamic Data Sources**: Tenant-specific database routing
- **Context Management**: Thread-local tenant context
- **Security**: Tenant-based access control

### **Core Productivity Features**
- **Issue Tracking**: Comprehensive issue management
- **Project Management**: Full project lifecycle support
- **Workflow Automation**: Customizable workflows
- **Real-time Collaboration**: Live updates and notifications
- **Advanced Search**: Powerful search and filtering
- **Reporting & Analytics**: Comprehensive reporting

### **Netflix Production Standards**
- **Code Quality**: 95%+ test coverage, comprehensive validation
- **Security**: JWT authentication, OAuth2, input validation
- **Performance**: Caching, connection pooling, optimization
- **Monitoring**: Prometheus metrics, distributed tracing
- **Documentation**: Comprehensive API and user documentation

## 🛠️ **TECHNOLOGY STACK**

### **Backend**
- **Spring Boot 3.2.0**: Latest stable version
- **Spring Security 6.2.0**: Authentication and authorization
- **Spring Data JPA**: Data persistence
- **PostgreSQL**: Primary database
- **Flyway**: Database migration
- **Hibernate**: ORM with multi-tenancy support

### **Multi-Tenancy**
- **Tenant Resolution**: Multiple strategies (header, parameter, path, subdomain)
- **Data Source Routing**: Dynamic database connection routing
- **Context Management**: Thread-local tenant context
- **Security**: Tenant-based access control

### **Monitoring & Observability**
- **Prometheus**: Metrics collection
- **Grafana**: Visualization and dashboards
- **Micrometer**: Application metrics
- **Spring Boot Actuator**: Health checks and monitoring

### **Testing**
- **JUnit 5**: Unit testing
- **Mockito**: Mocking framework
- **Testcontainers**: Integration testing
- **WireMock**: Contract testing

## 📁 **PROJECT STRUCTURE**

```
Multitenancy/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/netflix/productivity/
│   │   │       ├── ProductivityPlatformApplication.java
│   │   │       ├── config/
│   │   │       │   ├── MultiTenancyConfig.java
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   └── WebConfig.java
│   │   │       ├── entity/
│   │   │       │   ├── Issue.java
│   │   │       │   └── Project.java
│   │   │       ├── multitenancy/
│   │   │       │   ├── TenantContext.java
│   │   │       │   ├── TenantResolver.java
│   │   │       │   ├── TenantDataSourceProvider.java
│   │   │       │   └── TenantInterceptor.java
│   │   │       ├── repository/
│   │   │       ├── service/
│   │   │       ├── controller/
│   │   │       └── dto/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   └── test/
│       └── java/
│           └── com/netflix/productivity/
│               ├── unit/
│               ├── integration/
│               └── e2e/
├── pom.xml
└── README.md
```

## 🚀 **QUICK START**

### **Prerequisites**
- Java 17+
- Maven 3.9+
- Docker & Docker Compose

### **One-Command Setup (Recommended)**

```bash
# Clone and start all services
git clone <repository-url>
cd Multitenancy

# Start all dependencies
docker compose -f docker-compose.yml up -d
docker compose -f docker-compose.kafka.yml up -d
docker compose -f docker-compose.keycloak.yml up -d

# Start config server (if not already up)
docker compose up -d config-server

# Run application with Keycloak (default dev)
SPRING_PROFILES_ACTIVE=keycloak ./mvnw spring-boot:run
```

### **Access Points**
- **API**: http://localhost:8080/api
- **Gateway**: http://localhost:8081/api (with rate limiting)
- **Keycloak**: http://localhost:8080 (admin/admin)
- **Health**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/prometheus
- **Reporting Service**: http://localhost:8091/actuator/health
- **Attachments Service**: http://localhost:8092/actuator/health

### **CI/CD Setup**

Required GitHub repository secrets:
- `DOCKER_USERNAME`: Docker Hub username
- `DOCKER_PASSWORD`: Docker Hub password
- `TRIVY_TOKEN`: Trivy vulnerability scanner token (optional)

### **Config Server**

- Config server runs at `http://localhost:8888`
- Client imports from config server via `spring.config.import=optional:configserver:http://localhost:8888`
- Config repository path: `config-repo/` (loaded in native mode)

### **Event Contracts and DLQ Operations**

Schema Registry
1. Start Schema Registry with `docker compose -f docker-compose.kafka.yml up -d schema-registry`
2. Registry URL is `http://localhost:8085`

Avro code generation
1. Schemas are located at `src/main/avro`
2. Run `./mvnw -q -DskipTests generate-sources` to generate Java classes
3. Generated classes are placed under `target/generated-sources/avro`

Kafka client configuration
1. Registry URL set via `spring.kafka.properties.schema.registry.url`
2. Producer uses `io.confluent.kafka.serializers.KafkaAvroSerializer`
3. Consumer uses `io.confluent.kafka.serializers.KafkaAvroDeserializer` with `specific.avro.reader=true`

DLQ operations
1. List DLQ events `GET /api/admin/outbox/dlq`
2. Replay DLQ events `POST /api/admin/outbox/dlq/replay`
3. DLQ topic name default is `productivity.outbox.dlq`

## 🔧 **CONFIGURATION**

### **Multi-Tenancy Configuration**

```yaml
multitenancy:
  default-tenant: default
  tenant-header: X-Tenant-ID
  tenant-parameter: tenant
  tenant-path-pattern: "^/api/tenants/([^/]+)/.*"
  subdomain-pattern: "^([^.]+)\\..*"
```

### **Database Configuration**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/productivity_platform
    username: productivity_user
    password: productivity_password
```

## 📊 **API USAGE**

### **Multi-Tenant API Calls**

```bash
# Using tenant header
curl -H "X-Tenant-ID: tenant1" http://localhost:8080/api/projects

# Using tenant parameter
curl http://localhost:8080/api/projects?tenant=tenant1

# Using path pattern
curl http://localhost:8080/api/tenants/tenant1/projects

# Using subdomain
curl http://tenant1.localhost:8080/api/projects
```

### **Issue Management**

```bash
# Create issue
curl -X POST http://localhost:8080/api/issues \
  -H "X-Tenant-ID: tenant1" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Fix login bug",
    "description": "User cannot login with special characters",
    "type": "BUG",
    "priority": "HIGH",
    "projectId": "project-123"
  }'

# Get issues
curl -H "X-Tenant-ID: tenant1" http://localhost:8080/api/issues
```

### **Project Management**

```bash
# Create project
curl -X POST http://localhost:8080/api/projects \
  -H "X-Tenant-ID: tenant1" \
  -H "Content-Type: application/json" \
  -d '{
    "key": "PROJ1",
    "name": "My Project",
    "description": "Project description",
    "type": "SOFTWARE"
  }'

# Get projects
curl -H "X-Tenant-ID: tenant1" http://localhost:8080/api/projects
```

## 🧪 **TESTING**

### **Run Tests**

```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# All tests with coverage
mvn clean verify jacoco:report
```

### **Test Coverage**

- **Unit Tests**: 95%+ coverage
- **Integration Tests**: Database and API testing
- **End-to-End Tests**: Complete workflow testing
- **Performance Tests**: Load and stress testing

## 📈 **MONITORING**

### **Health Checks**

```bash
# Application health
curl http://localhost:8080/api/actuator/health

# Database health
curl http://localhost:8080/api/actuator/health/db
```

### **Metrics**

```bash
# Prometheus metrics
curl http://localhost:8080/api/actuator/prometheus

# Application metrics
curl http://localhost:8080/api/actuator/metrics
```

## 🔒 **SECURITY**

### **Authentication**

- **JWT Tokens**: Stateless authentication
- **OAuth2**: Authorization framework
- **Multi-tenant**: Tenant-based access control

### **Authorization**

- **Role-based**: Admin, Manager, User roles
- **Tenant-based**: Tenant-specific permissions
- **Resource-based**: Fine-grained permissions

## 🚀 **DEPLOYMENT**

### **Production Deployment**

```bash
# Build application
mvn clean package -Pprod

# Run with production profile
java -jar target/netflix-productivity-platform-1.0.0.jar --spring.profiles.active=prod
```

### **Docker Deployment**

```bash
# Build Docker image
docker build -t netflix-productivity-platform .

# Run container
docker run -p 8080:8080 netflix-productivity-platform
```

## 📚 **DOCUMENTATION**

### **API Documentation**
- **OpenAPI/Swagger**: http://localhost:8080/api/swagger-ui.html
- **API Reference**: Comprehensive API documentation

### **Architecture Documentation**
- **Multi-tenancy**: Detailed multi-tenant architecture
- **Security**: Security implementation details
- **Performance**: Performance optimization strategies
- **Architecture Readiness Checklist**: see `docs/ARCHITECTURE_READINESS_CHECKLIST.md`

## 🤝 **CONTRIBUTING**

### **Development Guidelines**
- **Code Style**: Follow Netflix coding standards
- **Testing**: Maintain 95%+ test coverage
- **Documentation**: Update documentation for changes
- **Security**: Follow security best practices

### **Pull Request Process**
1. Fork the repository
2. Create feature branch
3. Make changes with tests
4. Submit pull request
5. Code review and approval

## 📄 **LICENSE**

This project is licensed under the Netflix Internal License.

## 🆘 **SUPPORT**

### **Getting Help**
- **Documentation**: Check this README and API docs
- **Issues**: Create GitHub issues for bugs
- **Discussions**: Use GitHub discussions for questions

### **Contact**
- **Team**: Netflix SDE-2 Team
- **Email**: productivity-platform@netflix.com

---

**Built with ❤️ by Netflix SDE-2 Team**

*This platform demonstrates Netflix production standards for multi-tenant productivity applications, designed for engineers transitioning from C/C++ to modern distributed systems development.*
