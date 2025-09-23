# Sliding Window Log Rate Limiting - Production Notes

## Overview

Keep a time ordered log per key and evict entries older than window to enforce limits precisely.

## Implementation notes

- Use Redis sorted sets with timestamps as scores
- ZADD current timestamp; ZREMRANGEBYSCORE to evict old; ZCARD to count
- Wrap in Lua to ensure atomicity and reduce round trips

## Lua skeleton

```lua
-- KEYS[1] key, ARGV[1] nowMs, ARGV[2] windowMs, ARGV[3] limit
redis.call('ZREMRANGEBYSCORE', KEYS[1], '-inf', ARGV[1]-ARGV[2])
redis.call('ZADD', KEYS[1], ARGV[1], ARGV[1])
local c = redis.call('ZCARD', KEYS[1])
if c > tonumber(ARGV[3]) then return 0 else return 1 end
```

## Pros/Cons

- Precise but higher write amplification than token bucket

## Deep Dive Appendix

### Adversarial scenarios
- Boundary effects causing unfair allow deny near window edges
- High write amplification at large scale
- Clock skew between nodes enforcing limits

### Internal architecture notes
- Log of timestamps per key with pruning; approximate variants with buckets
- Distributed enforcement using Redis and Lua with hash tags
- Memory caps and eviction policies for inactive keys

### Validation and references
- Precision and fairness benchmarks vs token bucket and fixed window
- Load tests measuring storage and CPU
- Literature on windowed rate limiting

### Trade offs revisited
- Precision vs cost; fairness vs simplicity

### Implementation guidance
- Use where precision matters; cap memory; combine with edge token bucket
