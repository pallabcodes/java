# GraphQL Federation - Production Notes

## Overview

Compose multiple GraphQL subgraphs into a single federated graph for product APIs.

## Patterns

- Define entities with keys and reference resolvers
- Keep schemas backward compatible; version through deprecations
- Apply query cost limits and depth limits at gateway

## Pitfalls

- N+1 across services; use dataloader per subgraph
- Cyclic entity references; break with composition

## Deep Dive Appendix

### Adversarial scenarios
- N plus 1 resolver explosions and over fetching
- Schema composition conflicts and version drift
- Partial outages in subgraphs causing cascade failures

### Internal architecture notes
- Gateway composition, query planning, and caching
- Contract and ownership per subgraph; federation keys and references
- Timeouts, retries, and circuit breakers per resolver

### Validation and references
- Load tests on representative client queries and plans
- Contract tests and schema checks in CI
- Literature on federation, supergraphs, and query planning

### Trade offs revisited
- Client flexibility vs backend coupling; latency vs aggregation power

### Implementation guidance
- Enforce resolver budgets; cache and batch; monitor per field
- Versioned schema evolution with deprecation windows
