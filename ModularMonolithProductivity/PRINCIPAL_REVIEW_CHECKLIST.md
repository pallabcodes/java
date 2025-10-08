# Principal Review Checklist (Architecture)

## Boundaries and layering
- Controllers do not access repositories directly
- Application layer orchestrates use cases only
- Domain free of framework annotations beyond JPA if used
- No cross module entity references, ids and api only

## Multi tenancy
- Tenant resolution, filter activation, and constraints proven with tests
- Composite unique keys include tenant id on hot tables

## Performance
- Index plan documented with EXPLAIN for top five queries
- Pagination caps enforced; search limits enforced
- Caching policy defined per module

## Observability
- Structured logs include tenant id and request id
- Baseline metrics and a couple of domain counters

## Security
- Auth flow documented; roles and permissions mapped to actions
- Input validation at api layer

## Extraction readiness
- Module public api documented and versioned
- Adapters defined to allow remote replacement
