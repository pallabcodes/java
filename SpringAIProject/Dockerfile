# Multi-stage Docker build for Netflix SpringAI Service
# Stage 1: Build the application
FROM eclipse-temurin:17-jdk-alpine AS builder

# Install Maven
RUN apk add --no-cache maven

# Set working directory
WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

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
COPY --from=builder /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/health/live || exit 1

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
     "-Dspring.profiles.active=prod", \
     "-jar", \
     "app.jar"]