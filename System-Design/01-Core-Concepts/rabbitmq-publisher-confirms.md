# RabbitMQ Publisher Confirms - Production Notes

## Overview

Publisher confirms provide reliability for message publishing without transactions. The broker acknowledges publishes per message or in batches.

## Patterns

- Enable confirms on channel; use multiple outstanding publishes
- Track sequence numbers; on nack or timeout, retry idempotently
- Batch confirms to reduce overhead

## Pitfalls

- Per message confirms reduce throughput
- Ensure idempotent consumers to handle duplicates

## Deep Dive Appendix

### Adversarial scenarios
- Network partitions during publish confirms
- Broker restarts and transient nacks
- Publisher retry loops causing duplicates

### Internal architecture notes
- Confirm select, per message ack nack handling, and correlation ids
- Outbox pattern for transactional publish
- DLX and delayed retries for robust pipelines

### Validation and references
- Fault injection: broker restarts, link drops, and throttling
- Replay tests to validate dedup and idempotent processing
- RabbitMQ confirms literature and best practices

### Trade offs revisited
- Throughput vs confirm granularity; latency vs reliability

### Implementation guidance
- Use confirms with batching; include ids for dedup; DLQ and visibility on failures
