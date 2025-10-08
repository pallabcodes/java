# ADR-0001 Modular Monolith

## Context

Internal tool for up to one thousand users, high scrutiny, low ops budget, need for clear evolution path.

## Decision

Adopt a modular monolith architecture with strict boundaries and extraction seams. One deployable, multiple modules with api, application, domain, infrastructure layers.

## Consequences

- Faster delivery and debugging now
- Clear path to extract modules into services later
- Requires discipline enforced by ArchUnit and review

## Alternatives considered

- Microservices from day one: higher ops tax and slower delivery
- Classic monolith without boundaries: fast start but high future refactor cost
