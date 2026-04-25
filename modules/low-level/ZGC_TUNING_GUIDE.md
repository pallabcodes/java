# Generational ZGC Tuning Guide (Java 21 L7 Mastery)

In Java 21, **Generational ZGC** (JEP 439) is the state-of-the-art for low-latency systems (e.g., trading platforms, high-frequency microservices). It reduces memory overhead and CPU usage compared to non-generational ZGC.

## 1. Enabling Generational ZGC
To enable the next-generation collector in Java 21:

```bash
java -XX:+UseZGC -XX:+ZGenerational -Xmx16G YourApp
```

## 2. Key Concepts for L7 Engineers
- **The "Low Latency" Promise**: Sub-millisecond max pause times, regardless of heap size (from 8MB to 16TB).
- **Generational Hypotheses**: Most objects die young. By separating the heap into young and old generations, ZGC can collect short-lived objects more frequently with less overhead.
- **Colored Pointers & Load Barriers**: ZGC uses metadata bits in pointers to track object state without stopping the world.

## 3. Performance Tuning Flags

| Flag | Purpose | L7 Recommendation |
| :--- | :--- | :--- |
| `-Xms / -Xmx` | Heap Size | Set them to the same value to avoid dynamic heap resizing overhead. |
| `-XX:SoftMaxHeapSize` | Soft limit | Useful for containers; ZGC will try to stay below this without crashing. |
| `-XX:ZCollectionInterval` | Periodic GC | Force a GC every N seconds if there's no allocation pressure. |
| `-XX:ZAllocationSpikeTolerance` | Safety margin | Increase if your app has sudden, massive allocation spikes. |

## 4. Observability (Mechanical Sympathy)
To see what ZGC is actually doing under the hood, use the unified logging system:

```bash
java -Xlog:gc*:file=gc.log:time,level,tags -XX:+UseZGC -XX:+ZGenerational ...
```

### JFR Events
For deep analysis, ZGC emits specific JFR events:
- `jdk.GCPhasePause`: Track those sub-ms pauses.
- `jdk.ZAllocationStall`: Critical! Shows if threads are waiting for GC to free up space.

---
*L7 Insight: ZGC is a concurrent collector. If your threads are faster than the collector can free memory, you will hit an 'Allocation Stall'. Always monitor allocation rates, not just pause times.*
