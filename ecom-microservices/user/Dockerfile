# Multi-stage Docker build for Netflix E-commerce User Service
FROM eclipse-temurin:17-jdk-alpine AS builder

RUN apk add --no-cache maven
WORKDIR /app

COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .
RUN ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

RUN apk update && \
    apk upgrade && \
    apk add --no-cache dumb-init curl && \
    rm -rf /var/cache/apk/*

RUN addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
RUN chown -R appuser:appuser /app

USER appuser
EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["dumb-init", "--"]
CMD ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", \
     "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=docker", \
     "-jar", "app.jar"]
