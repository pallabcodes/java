# Bloom Filters - Netflix Production Guide

## 🎯 Concept overview

Bloom filters are probabilistic data structures to test set membership with no false negatives and controllable false positives.

## 📊 Implementation layer classification

| Component | Layer | Type | Netflix status |
|---|---|---|---|
| Client side Bloom | Application | request pre check | Production |
| Service side Bloom | Application | cache warmups, dedup | Production |
| Storage tier Bloom | Infrastructure | SSTable level filters | Production |

## 🚀 Production implementations

- Pre check keys before cache or DB lookups to reduce misses
- Topic partition dedup in streaming systems
- Storage engines use per segment filters for IO reduction

## 🧭 Production Readiness Addendum

### Techniques and where to use
- Front caches to avoid negative lookups
- Deduplicate events by tested membership of processed ids
- Tune bits per element and number of hash functions based on FPR target

### Trade offs
- Latency: constant time O(k) checks
- Memory: proportional to n * bits per element
- Network: share filter snapshots across nodes when needed
- Process: periodic rebuilds to manage growth
- Cost: small memory cost for large IO savings
- Complexity: probabilistic semantics with false positives

### Quantified trade offs
* Bits per element: for p false positive rate, m ≈ -n ln p ÷ (ln 2)^2. For n 10 million and p 1 percent, m ≈ 96 million bits ≈ 12 MB.
* Hash functions: k ≈ (m ÷ n) ln 2. With m ÷ n = 9.6, k ≈ 7. Optimize by double hashing.
* CPU: 5 to 10 hashes per lookup cost under 0.5 microseconds on modern CPUs for small keys.
* Growth: once fullness approaches 50 percent bits set, FPR rises faster; rebuild or use scalable blooms.

### Failure modes and mitigations
- Elevated FPR when overloaded: rebuild with more bits
- Stale filters causing misses: periodic refresh from source of truth
- Hash function bias: use independent hash derivation

### Sizing and capacity
- m = ceil((-n * ln p) / (ln 2)^2), k = round((m/n) * ln 2)
- Choose n from peak unique keys per window, p from acceptable FPR

### Verification
- Measure observed FPR with sampled ground truth
- Load tests with skewed distributions

### Production checklist
- Metrics: FPR, TPR, memory use, rebuild time
- Alerts: FPR above threshold, rebuild failures
- Runbooks: rebuild filter, rotate snapshots, adjust parameters

## 📊 Technique Trade offs Matrix (Internal)

| Technique | Latency | Memory | FPR | Cost | Complexity | Notes |
|---|---|---|---|---|---|---|
| Standard Bloom | very low | medium | configurable | low | low | good default |
| Partitioned Bloom | very low | medium | lower | medium | medium | per shard filters |
| Counting Bloom | low | higher | configurable | medium | medium | supports deletes |

## Deep Dive Appendix

### Adversarial scenarios
- Rapid growth beyond planned n increasing FPR beyond target
- Skewed key distributions and adversarial inputs
- Version drift between producers and consumers interpreting filters

### Internal architecture notes
- Double hashing to derive k hash values efficiently
- Partitioned filters per shard or tenant to localize FPR and updates
- Scalable bloom filters for unbounded growth with staged arrays

### Validation and references
- Empirical FPR measurement against ground truth samples
- Load tests with Zipfian and adversarial distributions
- Classic bloom filter literature and scalable variants

### Trade offs revisited
- Memory vs FPR vs CPU for hashing and rebuild cadence

### Implementation guidance
- Set p from product tolerance, compute m and k, and budget memory
- Periodically rebuild or roll filters to cap FPR; snapshot and rotate safely
