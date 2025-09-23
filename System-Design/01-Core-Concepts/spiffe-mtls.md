# SPIFFE mTLS - Production Notes

## Overview

SPIFFE/SPIRE issues workload identities (SVIDs) and automates mTLS between services without managing long lived secrets.

## Components

- SPIRE server and agents
- Workload API for SVID distribution
- mTLS libraries (Envoy, gRPC, Java TLS) consuming SVIDs

## Patterns

- Identity based authorization: authorize by SPIFFE ID instead of network
- Short lived certs with automatic rotation
- Pin trust domain per environment

## Pitfalls

- Ensure clock sync across nodes
- Plan for agent availability and fallback

## Deep Dive Appendix

### Adversarial scenarios
- CA outages and stale identities
- Name constraints and trust domain misconfigurations
- Certificate rotation failures causing outages

### Internal architecture notes
- SPIFFE IDs, SVID issuance, and workload API
- Mutual TLS handshakes, trust bundles, and rotation cadence
- Policy enforcement with service identity and RBAC

### Validation and references
- Chaos on CA and bundle distribution; expiry drills
- Interop tests across runtimes and languages
- Literature on SPIFFE and zero trust

### Trade offs revisited
- Security strength vs operational complexity; rotation frequency vs stability

### Implementation guidance
- Automate issuance and rotation; monitor expiry; validate policies in CI
