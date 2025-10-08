# Folder Structure

```
ModularMonolithProductivity/
  docs/
  modules/
    core/
      api/
      application/
      domain/
      infrastructure/
    tenants/
      api/
      application/
      domain/
      infrastructure/
    projects/
      api/
      application/
      domain/
      infrastructure/
    issues/
      api/
      application/
      domain/
      infrastructure/
    search/
      api/
      application/
      domain/
      infrastructure/
  platform/
    security/
    observability/
    cache/
  build/
  scripts/
```

## Notes

- modules own their boundaries and depend inward only
- platform holds shared kernel elements with strict review gate
