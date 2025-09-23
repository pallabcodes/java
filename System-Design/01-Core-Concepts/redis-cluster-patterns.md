# Redis Cluster Patterns - Production Notes

## Goals

- Horizontal scale with Redis Cluster (hash slots)
- Client side partition awareness and retries
- Key design for multi key operations and pipelining

## Patterns

- Slot affinity: group related keys with hash tags, example `user:{42}:profile`
- Multi key ops: ensure all keys share the same hash tag
- Pipelining: batch ops per node; avoid cross node pipelines
- Failover: use client with cluster topology refresh and MOVED/ASK handling

## Client configuration

- Enable topology refresh (adaptive and periodic)
- Configure timeouts and max redirects for MOVED/ASK
- Use connection pooling per node

## Pitfalls

- Large values cause hot spots; compress where appropriate
- Keys without tags break multi key ops across slots
- Heavy scripts may block single threaded server

## References

- Redis cluster specification
- Client docs for Lettuce/Jedis cluster

## Deep Dive Appendix

### Adversarial scenarios
- MOVED storms during slot migration
- Hot key skew within a slot causing saturation
- Gossip convergence delays on large clusters

### Internal architecture notes
- Hash slot mapping and client side topology cache
- Multi key ops with hash tags and Lua for atomicity
- Resharding controller with rate limits and health gates

### Validation and references
- Slot move drills with synthetic load and tail tracking
- Node failure and link throttling tests
- Literature on consistent hashing and cluster management

### Trade offs revisited
- Operational churn vs evenness; replica count vs memory cost

### Implementation guidance
- Pre warm added nodes; cap concurrent slot moves; monitor MOVED ASK rates
- Enforce value schemas and versioned migrations
