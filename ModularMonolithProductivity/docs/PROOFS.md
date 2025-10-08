# Proof Pack for Principal Review

## SQL plans

- Provide EXPLAIN for list projects with filters and paging
- Document indexes: projects(key), projects(name)

## Transaction boundaries

- Application layer orchestrates and opens transactions
- Controllers remain thin without transaction management

## Metrics

- http server requests p95 per endpoint
- domain counters per module like projects.created.count

## Module ownership

- projects: owner sde3
- tenants: owner sde3
- issues: owner sde3
