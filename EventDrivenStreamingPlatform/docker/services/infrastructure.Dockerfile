# Event-Driven Streaming Infrastructure Service Dockerfile
# Multi-stage build for optimized production image

# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml for dependency caching
COPY infrastructure/pom.xml infrastructure/pom.xml
COPY shared-events/pom.xml shared-events/pom.xml
COPY pom.xml .

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -pl infrastructure,shared-events

# Copy source code
COPY infrastructure/src ./infrastructure/src
COPY shared-events/src ./shared-events/src

# Build the application
RUN ./mvnw clean package -pl infrastructure,shared-events -am -DskipTests \
    && cp infrastructure/target/*.jar app.jar

# Runtime stage
FROM eclipse-temurin:21-jre-alpine AS runtime

# Install necessary packages for health checks and monitoring
RUN apk add --no-cache \
    curl \
    dumb-init \
    && rm -rf /var/cache/apk/*

# Create non-root user for security
RUN addgroup -S streaming && adduser -S streaming -G streaming

WORKDIR /app

# Copy the built application
COPY --from=builder /app/app.jar .

# Change ownership to non-root user
RUN chown -R streaming:streaming /app
USER streaming

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

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
      description="Event-Driven Streaming Infrastructure Service" \
      service="infrastructure" \
      component="event-bus"

# Security: Don't run as root
USER streaming