# Architecture

## Tenets

- Modules own their data, api, and internal services
- Only public facades are callable across modules
- No cross module entity references, use ids and api calls
- Shared kernel is small and stable

## Layers per module

- api: controllers and request response shapes
- application: services, orchestrations, validation
- domain: entities, aggregates, repositories interface
- infrastructure: db mappings, adapters, messaging clients

## Cross cutting

- security: auth, roles, permissions
- observability: logging, metrics, tracing
- platform: caching, rate limiting, pagination caps

## Seams for extraction

- module api → rest facade that can be lifted out behind a gateway
- repository interface → can swap to separate datastore through adapter
- events → domain events to decouple write and read paths later

## Data boundaries

- single database with strict tenant column and hibernate filter
- composite unique keys include tenant id
- jsonb for custom fields with a few indexed paths

## Performance guidance

- explicit indexes for hot predicates
- capped page sizes and search limits
- small in memory caches per tenant with ttl

## Failure modes

- graceful degradation on search and reports
- circuit limits on integrations
