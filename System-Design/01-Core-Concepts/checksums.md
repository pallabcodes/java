# Checksums - Netflix Production Guide

## 🎯 Concept overview

Checksums detect data corruption and verify integrity across storage, network, and processing stages.

## 📊 Implementation layer classification

| Component | Layer | Type | Netflix status |
|---|---|---|---|
| Transport checksum | Infrastructure | TCP, TLS MAC | Production |
| Storage checksum | Infrastructure | block/object checks | Production |
| Application checksum | Application | payload integrity | Production |

## 🚀 Production implementations

- End to end checksums on large objects, verified at write and read
- Rolling checksums for streaming and chunked data
- Merkle trees for large datasets and replication verification

## 🧭 Production Readiness Addendum

### Techniques and where to use
- Strong hashes for integrity verification and deduplication
- CRC for fast detection at block and packet layers
- Per chunk checksums with offsets for resumable transfers

### Trade offs
- Latency: hashing cost measured per MB; choose algorithms accordingly
- CPU: strong hashes are CPU intensive; offload with SIMD where possible
- Network: include checksums in headers or metadata
- Process: end to end verification increases reliability
- Cost: storage for checksum metadata
- Complexity: partial verification and rolling windows

### Quantified trade offs
* CPU throughput: CRC32C with HW acceleration processes 5 to 15 GB per second per core; SHA256 300 to 800 MB per second per core; BLAKE3 2 to 5 GB per second per core.
* Storage overhead: 32 to 256 bits per chunk; for 4 MB chunks and 128 bit checksum, overhead ≈ 0.4 ppm; negligible compared to data.
* Streaming: per 4 KB block checksums add under 1 microsecond per block with CRC; choose per chunk size based on repair granularity vs metadata.

### Failure modes and mitigations
- Silent corruption in transit or at rest: end to end checksum validation
- Hash collision risk: use modern strong hashes for critical paths
- Mismatch handling: automatic retry or repair from replica

### Sizing and capacity
- Budget CPU per MB for hashing at ingress and egress
- Metadata storage for checksums per object or block

### Verification
- Inject bit flips and verify detection and recovery paths
- Cross validate with independent implementations

### Production checklist
- Metrics: checksum failure rate, repair rate, hash throughput
- Alerts: spikes in failures, repair backlog
- Runbooks: retransfer, repair from replica, quarantine bad segments

## 📊 Technique Trade offs Matrix (Internal)

| Technique | Detection Strength | CPU Cost | Storage | Complexity | Notes |
|---|---|---|---|---|---|
| CRC32/64 | medium | low | low | low | transport and blocks |
| SHA256 | very high | high | low | low | end to end integrity |
| Blake3 | very high | medium | low | low | fast and parallel |
| Merkle trees | very high | high | medium | medium | large datasets |

## Deep Dive Appendix

### Adversarial scenarios
- Silent data corruption in transit or at rest
- Partial writes and torn reads on crash
- Malicious tampering vs accidental corruption

### Internal architecture notes
- End to end checksums stored with objects and verified on read
- Rolling checksums for streaming and chunked transfers
- Merkle trees to localize differences across large datasets

### Validation and references
- Bit flip injection and verification of detection and repair
- Cross implementation verification of hash outputs
- Literature on error detection codes and cryptographic hashes

### Trade offs revisited
- Detection strength vs CPU cost vs storage overhead

### Implementation guidance
- Choose CRC for transport and block layers; SHA256 or BLAKE3 for E2E integrity
- Define repair workflows using replicas; quarantine on repeated failures
