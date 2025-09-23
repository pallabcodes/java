# Range Sharding - Production Notes

## Overview

Range sharding partitions data by contiguous key ranges (for example by user id or time). It enables efficient range scans and time based retention but requires careful rebalancing planning.

## Key points

- Choose boundaries to avoid hot partitions
- Keep a directory of ranges to shard id mapping
- On split/merge, migrate keys in batches with backfill and cutover
- Use dual read during migration windows

## Example directory

```text
[0, 1_000_000) -> shard-a
[1_000_000, 2_000_000) -> shard-b
[2_000_000, 3_000_000) -> shard-c
```

## Migration sketch

- Provision new shard and add empty range
- Backfill data in id order with rate limiting
- Dual write new traffic
- Cutover reads when lag is near zero

## Monitoring

- Keys per range, write rate per range
- Split candidates based on utilization and p99 latency

## Pitfalls

- Uneven growth when key space is skewed
- Complex multi range queries

## Deep Dive Appendix

### Adversarial scenarios
- Skewed ranges creating hot shards
- Range boundary churn during growth
- Cross range queries causing fan out

### Internal architecture notes
- Boundary selection, split and merge operations
- Secondary lookup indexes for efficient range scans
- Metadata distribution and router caching

### Validation and references
- Heatmap analysis and boundary tuning with replay traces
- Split merge rehearsals under load
- Literature on range partitioning and rebalancing

### Trade offs revisited
- Scan performance vs hotspot risk; operational churn vs locality benefits

### Implementation guidance
- Start with generous ranges; automate detection and split; avoid cross range joins
