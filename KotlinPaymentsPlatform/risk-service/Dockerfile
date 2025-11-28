# Multi-stage Docker build for Kotlin Payments Platform - Risk Service
# Stage 1: Build the application
FROM eclipse-temurin:17-jdk-alpine AS builder

# Install Kotlin and Gradle
RUN apk add --no-cache gradle kotlin

# Set working directory
WORKDIR /app

# Copy build files
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle/ gradle/

# Copy source code
COPY src ./src

# Build the application
RUN gradle clean build -x test

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre-alpine

# Install security updates and required packages
RUN apk update && \
    apk upgrade && \
    apk add --no-cache \
        dumb-init \
        curl \
        && \
    rm -rf /var/cache/apk/*

# Create non-root user for security
RUN addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8083

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8083/api/health || exit 1

# Use dumb-init to handle signals properly
ENTRYPOINT ["dumb-init", "--"]

# Run the application
CMD ["java", \
     "-XX:+UseContainerSupport", \
     "-XX:MaxRAMPercentage=75.0", \
     "-XX:+UseG1GC", \
     "-XX:+UseCompressedOops", \
     "-XX:+UseCompressedClassPointers", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "-Dspring.profiles.active=docker", \
     "-jar", \
     "app.jar"]
