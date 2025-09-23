# Build stage
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY . .
RUN ./mvnw -q -DskipTests package || mvn -q -DskipTests package

# Run stage
FROM gcr.io/distroless/java17-debian12
WORKDIR /app
COPY --from=build /workspace/target/*.jar /app/app.jar
EXPOSE 8080
USER nonroot
ENTRYPOINT ["/usr/bin/java","-jar","/app/app.jar"]
