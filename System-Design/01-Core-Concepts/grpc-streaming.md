# gRPC Streaming - Production Notes

## Overview

Use client, server, or bidirectional streaming for real time updates and backpressure aware pipelines.

## Patterns

- Deadlines on calls; cancel on client side when exceeded
- Flow control: respect onReady, send in batches
- Authentication with mTLS and per call authz

## Pitfalls

- Head of line blocking if single stream carries unrelated work
- Unbounded buffers; enforce limits

## Deep Dive Appendix

### Adversarial scenarios
- Backpressure and flow control misconfig causing memory bloat
- Head of line blocking on single streams
- Connection churn on network flaps

### Internal architecture notes
- Sliding window flow control and credit based backpressure
- Bidirectional streaming with per stream queues and limits
- Keepalive, ping, and idle timeouts tuning

### Validation and references
- Churn tests with reconnects and network impairment
- Throughput vs latency benchmarks under mixed message sizes
- Literature on HTTP 2, flow control, and gRPC internals

### Trade offs revisited
- Latency vs batching; concurrency vs per stream ordering

### Implementation guidance
- Set conservative flow control windows; bound queues; drop policies for slow consumers
- Health checks and reconnect jitter to avoid storms
