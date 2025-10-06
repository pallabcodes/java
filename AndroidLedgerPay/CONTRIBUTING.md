# Contributing

## Branching
- main: protected
- feature/*: for features
- fix/*: for bug fixes

## Commits
- Conventional style: feat:, fix:, docs:, test:, chore:
- Keep changes focused; include rationale in body

## Code Style & Checks
- Kotlin style: ktlint, detekt
- Pre-commit hook is provided: run `git config core.hooksPath .githooks`
- Local checks:
  - `cd AndroidLedgerPay && ./gradlew ktlintCheck detekt lint testDebugUnitTest`

## Tests
- Unit tests required for non-trivial logic
- For data/Room tests use in-memory DB
- Compose UI tests for user flows when feasible

## Reviews
- Small, reviewable PRs preferred
- Add reviewers per CODEOWNERS

