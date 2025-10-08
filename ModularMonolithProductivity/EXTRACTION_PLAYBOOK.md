# Microservice Extraction Playbook

## When to extract

- module hot path saturates cpu or database independently
- independent deploy cadence demanded by product
- cross module changes become bottlenecks

## How to extract

1) Freeze module public api and add versioned facade under `/api/<module>/v1`
2) Introduce async outbound events for write paths where read latency can tolerate
3) Provide an in process adapter and a remote http adapter behind the same interface
4) Move persistence to its own schema first; swap the adapter to remote once stable
5) Add gateway route and service discovery as needed; keep client resilient with retries and circuit limits

## Data and contracts

- publish OpenAPI and a small conformance test
- version every breaking change; keep a deprecation window

## Rollout

- dark launch with mirrored traffic when possible
- canary, observe p95 and error rate, roll back on SLO burn
