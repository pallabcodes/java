# Principal Review Checklist

Use this checklist to quickly audit each concept doc for Netflix production readiness.

## Coverage
- Concept overview present and accurate
- Implementation layer classification table complete
- Production implementations listed and realistic

## Production Readiness Addendum
- Techniques and where to use them
- Trade offs across latency, availability, network, process, OS, cost, complexity
- Failure modes and mitigations
- Sizing and capacity guidance
- Verification strategy
- Production checklist with metrics, alerts, runbooks
- Internal trade offs matrix present

## Cross references
- Links to related concepts and implementations
- Notes on when this approach fails or should not be used

## Code and examples
- Multiple implementation approaches where applicable
- Clear comments and production defaults

## Observability and security
- Metrics and tracing guidance
- Authn, authz, and data protection notes

## Rollout
- Canary and rollback plan noted
- Config change safety practices

Status: pass | needs changes | fail
