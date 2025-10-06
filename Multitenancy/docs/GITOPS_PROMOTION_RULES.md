## GitOps promotion rules

- Branching
  - feature -> dev
  - dev -> staging via PR and CI green
  - staging -> prod via change window approval

- Gates
  - Pact provider and consumer
  - SAST and secrets scan
  - License check
  - Soak min 1h for canary

- Freeze windows
  - Weekdays 17:00-09:00 local
  - Weekend unless emergency

- Rollback
  - Auto on burn-rate exceed
  - Documented in canary workflow


