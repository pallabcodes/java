# Leaky Bucket at Gateway - Production Notes

## Overview

Shape traffic at the edge by draining tokens at a steady rate and queuing bursts within bounds.

## Implementation notes

- Use NGINX/Haproxy rate modules where possible (cheaper)
- For app gateways, maintain per key queue with max length and drain at fixed rate
- Apply priority for critical routes

## Pros/Cons

- Smooths bursts but adds latency when queueing
- Good complement to token bucket for fairness

## Deep Dive Appendix

### Adversarial scenarios
- Bursty traffic exceeding smoothing capacity
- Shared buckets across tenants causing unfairness
- Clock skew impacting refill rates

### Internal architecture notes
- Leaky bucket queue with constant drain rate and burst caps
- Hierarchical buckets per tenant and per route
- Integration with gateway policies and backpressure

### Validation and references
- Replay tests comparing smoothing and tail latency improvements
- Fault injection for gateway latency and drop behavior
- Literature on rate control algorithms

### Trade offs revisited
- Smoothing vs added latency; fairness vs complexity

### Implementation guidance
- Set per tenant buckets; expose Retry After; monitor queue length and drops
