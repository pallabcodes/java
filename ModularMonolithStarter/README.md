# Modular Monolith Starter

A generator friendly starter for building modular monoliths with selectable architecture presets and optional capsules. It emits a new repository shaped for speed while preserving clean seams for future extraction.

## Usage (phase one)

- Choose an architecture preset under `presets/` and capsules under `capsules/`
- Use the Cookiecutter skeleton in `cookiecutter/` to generate a new project with flags

## Presets

- layered
- hex
- clean
- feature
- cqrs

## Capsules

- security
- multitenancy
- observability
- db
- messaging

## Goals

- Fast day one productivity
- Style selectable and enforced with matching rules
- Minimal scaffolding that teams can extend
