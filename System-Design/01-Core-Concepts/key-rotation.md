# Key Rotation - Production Notes

## Overview

Rotate cryptographic keys regularly with dual read/write to avoid downtime.

## Patterns

- Envelope encryption with DEKs encrypted by rotating KEKs
- Staged rollout: write with new, read try new then old
- Maintain key version metadata alongside ciphertext

## Pitfalls

- Long lived caches returning stale key version
- Mixed versions during rollout; ensure reads are tolerant

## Deep Dive Appendix

### Adversarial scenarios
- Stale keys used by lagging services
- Rollback after key compromise
- Mixed mode during rotation windows

### Internal architecture notes
- Key versioning, dual encrypt decrypt during rotation
- KMS integration, envelope encryption, and access policies
- Audit trails and anomaly detection

### Validation and references
- Rotation drills and forced key revocation tests
- Decrypt replay across versions
- Literature on key management best practices

### Trade offs revisited
- Rotation frequency vs operational load; crypto strength vs performance

### Implementation guidance
- Define rotation cadence; automate and monitor; support emergency rotate and revoke
