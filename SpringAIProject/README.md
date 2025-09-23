# SpringAIProject

Production grade Spring AI service with chat and embeddings endpoints.

## Setup
- Java 17
- Maven
- Set OPENAI_API_KEY env var or rely on default arbitrary key

## Build and run
```
mvn spring-boot:run -pl SpringAIProject -am
```

## Endpoints
- POST /api/chat { "input": "hello" }
- POST /api/embeddings { "text": "hello" }

## Observability
- Actuator endpoints exposed at /actuator
- Correlation id header X-Correlation-Id supported