# Netflix Spring Framework Demo - Production Dockerfile
# This Dockerfile demonstrates production-grade containerization

# Multi-stage build for optimization
FROM maven:3.9.6-openjdk-17-slim AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build application
COPY src ./src
RUN mvn clean package -DskipTests

# Production stage
FROM openjdk:17-jre-slim

# Install necessary packages
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    curl \
    wget \
    && rm -rf /var/lib/apt/lists/*

# Create application user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy application jar
COPY --from=build /app/target/spring-framework-demo-1.0.0.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Application properties
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
ENV MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
