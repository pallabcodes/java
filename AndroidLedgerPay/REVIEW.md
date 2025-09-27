# AndroidLedgerPay – Reviewer Guide

## Overview
Production-grade Android Kotlin app scaffold demonstrating:
- Jetpack Compose + Material 3 UI
- Hilt DI across modules
- Modular architecture (core-network, core-data, core-ui, feature modules)
- Navigation graph with bottom navigation
- Persistence with Room; offline-first stubs
- Retrofit/OkHttp client with logging + timeout
- Unit tests (example ViewModel test)
- CI: build, unit tests, ktlint, detekt

## Modules
- app: Application, DI modules, navigation scaffold, screens wiring
- core-network: Retrofit ApiClient, PaymentsApi, OkHttp config
- core-data: Room entities/DAO/DB, repositories
- core-ui: shared Compose components
- feature-payments: Payments screen + ViewModel
- feature-ledger: Ledger screen stub

## How to run
- Android Studio: File → Open → `AndroidLedgerPay/` and Run `app`
- CLI (requires JDK 17):
  - `cd AndroidLedgerPay && ./gradlew assembleDebug`
  - `./gradlew testDebugUnitTest`

## CI
GitHub Actions workflow `.github/workflows/android.yml` runs:
- assembleDebug
- unit tests
- ktlintCheck
- detekt

## Key flows
- Home → Payments: create intent → persists to Room → list updates
- Network: `PaymentsApi` (placeholder base URL) with logging/timeout

## Next steps (optional)
- Add feature-ledger UI and tests
- Add DataStore for preferences and secure storage hooks
- Add more tests: repository with in-memory Room, UI tests on emulator
- Replace base URL and add auth/interceptors when backend is ready

