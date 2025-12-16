# Analytics Service Production Dockerfile
# Multi-stage build for optimized production image

# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml for dependency caching
COPY analytics-service/pom.xml analytics-service/pom.xml
COPY shared-events/pom.xml shared-events/pom.xml
COPY pom.xml .

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -pl analytics-service,shared-events

# Copy source code
COPY analytics-service/src ./analytics-service/src
COPY shared-events/src ./shared-events/src

# Build the application
RUN ./mvnw clean package -pl analytics-service,shared-events -am -DskipTests \
    && cp analytics-service/target/*.jar app.jar

# Runtime stage
FROM eclipse-temurin:21-jre-alpine AS runtime

# Install necessary packages for health checks and monitoring
RUN apk add --no-cache \
    curl \
    dumb-init \
    && rm -rf /var/cache/apk/*

# Create non-root user for security
RUN addgroup -S analytics && adduser -S analytics -G analytics

WORKDIR /app

# Copy the built application
COPY --from=builder /app/app.jar .

# Change ownership to non-root user
RUN chown -R analytics:analytics /app
USER analytics

# Expose port
EXPOSE 8083

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8083/actuator/health || exit 1

# Use dumb-init to handle signals properly
ENTRYPOINT ["dumb-init", "--"]
CMD ["java", \
     # JVM settings for containerized environments
     "-XX:+UseContainerSupport", \
     "-XX:MaxRAMPercentage=75.0", \
     "-XX:+UseG1GC", \
     "-XX:+UseStringDeduplication", \
     "-XX:+OptimizeStringConcat", \
     # Application settings
     "-Djava.security.egd=file:/dev/./urandom", \
     "-Dspring.profiles.active=docker", \
     # Enable remote debugging (optional)
     # "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", \
     "-jar", \
     "app.jar"]

# Labels for container metadata
LABEL maintainer="Netflix Streaming Platform Team" \
      version="1.0.0" \
      description="Real-Time Analytics Service" \
      service="analytics" \
      component="analytics-engine"

# Security: Don't run as root
USER analytics
