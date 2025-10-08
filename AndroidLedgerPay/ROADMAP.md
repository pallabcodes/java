# AndroidLedgerPay Roadmap

## Goals
Deliver a production-grade Android payments and ledger client with strong security defaults, modular architecture, high test coverage, and review-friendly tooling.

## Near-term (next 1-2 weeks)
- Network & Security
  - Environment-driven base URL wiring via DataStore + build flavors
  - Per-flavor certificate pinning configuration and rotation guidance
- Payments UX
  - Intent details screen (status timeline, retry)
  - Background sync with WorkManager for refresh and reliability
- Data & Offline
  - Room schema expansion (indexes, relations); Paging for lists
  - DAO test suite expansion
- Observability & Perf
  - Baseline Profiles + Macrobenchmark wired in CI after UI stabilizes
  - Structured client events (non-PII) and crash/perf hooks behind flags
- Accessibility & i18n
  - Content descriptions audit, large-text and TalkBack checks
  - Additional locale pack and RTL validation

## Mid-term (2-4 weeks)
- Auth scaffolding (token storage, auth interceptor)
- Feature flags & developer diagnostics panel
- Deep links and saved-state restoration across process death
- DI-driven UI test flavors with fake repos for deterministic error testing

## CI & Quality Gates
- Emulator tests on PRs for Payments happy path and error flows
- Dependency updates automation (Renovate/Versions plugin)
- Enforce lint zero warnings for app and libraries

## Deliverables
- Docs: updated REVIEW, CONTRIBUTING, architecture diagram, API integration guide
- Demo script: end-to-end Payments demo steps for reviewers

## Owners
- Codeowners file defines review team; assign module leads per feature as we expand.
