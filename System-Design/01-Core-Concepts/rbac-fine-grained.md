# Fine Grained RBAC - Production Notes

## Overview

Model permissions at resource and action level, evaluate at request time with attributes (ABAC+RBAC).

## Patterns

- Role -> permission sets, permission = resource:action
- Tenant aware policies
- Context attributes (owner, region, sensitivity) for ABAC conditions

## Pitfalls

- Policy sprawl; keep policies composable and testable
- Cache with short TTL; invalidate on role changes

## Deep Dive Appendix

### Adversarial scenarios
- Privilege escalation via mis scoped roles
- Orphaned permissions and stale grants
- Policy conflicts across services

### Internal architecture notes
- Role, permission, and attribute models; policy evaluation
- Least privilege defaults and time bound grants
- Audit logging and review workflows

### Validation and references
- Policy tests and simulation; access review automation
- Attack path analysis and privilege discovery
- Literature on RBAC ABAC and policy engines

### Trade offs revisited
- Granularity vs manageability; performance vs expressiveness

### Implementation guidance
- Central policy service; standardized checks; periodic reviews and cleanup
