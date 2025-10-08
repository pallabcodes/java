# Module Boundaries

## Allowed dependencies

- api to application
- application to domain and platform
- domain to java libs only
- infrastructure to domain and platform

## Forbidden

- api to repository direct calls
- cross module entity references
- domain depending on framework specific classes

## Review checklist

- Public facades are the only cross module entry points
- DTOs in api, entities in domain, mappers at the edge
- Tests prove no cycles between modules
