# Postgres Advisory Locking - Production Notes

## Overview

Postgres advisory locks are application managed, lightweight locks identified by integers. They are session scoped and survive process level logic within the same connection.

## When to use

- Per resource mutual exclusion without external lock service
- Short critical sections guarded at the database layer

## Patterns

- Use `pg_try_advisory_lock(key)` for non blocking acquire
- Always pair with `pg_advisory_unlock(key)` in finally blocks
- Map resource id to a 64 bit key (two int32 or one int64)

## Example

```sql
select pg_try_advisory_lock(42);
-- work
select pg_advisory_unlock(42);
```

## Pitfalls

- Locks are per session; ensure connection is not reused unexpectedly mid critical section
- Do not hold across long transactions

## Deep Dive Appendix

### Adversarial scenarios
- Session disconnects leaving locks held until timeout
- Long transactions blocking advisory lock acquisition
- Clock skew impacting lease semantics when used with timeouts

### Internal architecture notes
- Advisory locks scoped to session and transaction; key hashing strategies
- Combined with application fencing tokens for safety
- Monitoring lock tables and wait events

### Validation and references
- Load tests with competing lock holders and failures
- Verification of fencing and unlock semantics
- Literature on Postgres locking and advisory locks

### Trade offs revisited
- Simplicity and locality vs cross process semantics; not a distributed consensus substitute

### Implementation guidance
- Use for coarse grained mutex within a database; keep TTL short and operations idempotent
