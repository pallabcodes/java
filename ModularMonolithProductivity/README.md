# Modular Monolith Productivity Platform

This defines a modular monolith architecture for an internal productivity tool serving up to one thousand users. The design emphasizes clear module boundaries, enforceable layering, and extraction seams so any module can be turned into a microservice later with minimal refactor.

## Goals

- Deliver a single deployable with strong module boundaries
- Keep ops footprint minimal and easy to debug
- Provide clear seams for future extraction to services
- Ensure tenant safety, performance, and maintainability

## Non goals

- Internet scale multi region active active on day one
- Complex service mesh and distributed config

## Contents

- ARCHITECTURE.md
- FOLDER_STRUCTURE.md
- EXTRACTION_PLAYBOOK.md
- PRINCIPAL_REVIEW_CHECKLIST.md
- ARCHUNIT_RULES.md
- docs/OWNERS.md
