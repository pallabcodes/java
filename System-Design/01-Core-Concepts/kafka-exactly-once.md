# Kafka Exactly Once Semantics - Production Notes

## Overview

Use idempotent producers and transactions to achieve exactly once processing in Kafka (within Kafka boundaries). Downstream sinks must be idempotent or participate in the transaction (Kafka Streams/Connect).

## Producer config

- enable.idempotence=true
- acks=all
- max.in.flight.requests.per.connection=1..5 (keep small)
- retries>0
- transactional.id set per instance

## Flow

- Init transactions (producer.initTransactions)
- Begin transaction, send records, send offsets to transaction
- Commit or abort

## Caveats

- EOS guarantees do not extend to external systems without transactional integration
- Avoid long transactions; keep batches bounded

## Deep Dive Appendix

### Adversarial scenarios
- Producer restarts mid transaction
- Broker leader changes and ISR shrink during transactions
- Cross partition ordering assumptions breaking projections

### Internal architecture notes
- Idempotent producer, transactional producer, and fencing via PID and epochs
- Read process write with EOS using transactions and offsets in the same topic
- Choreography with outbox for non transactional sinks

### Validation and references
- Chaos on broker and network; transaction abort and retry coverage
- Replay correctness tests verifying exactly once effect
- Kafka literature on idempotence and transactions

### Trade offs revisited
- Complexity and latency overhead vs dedup consumer simplicity

### Implementation guidance
- Reserve EOS for money movement and critical effects; default to at least once with idempotent consumers
