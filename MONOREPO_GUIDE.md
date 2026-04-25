# Java 21 Mastery: L7 Systems Engineering Knowledgebase

An authoritative, runnable reference for modern Java 21+ — architected for senior systems engineers (L5/L7) who demand mechanical sympathy and zero wasted time.

## 🚀 One-Command Quickstart

```bash
# 1. Install local JDK 21 & Maven (no sudo)
bash scripts/setup.sh

# 2. Build all modules and run every demo
bash scripts/build.sh
```

---

## 🏗️ Monorepo Architecture

```text
java/
├── .tools/              ← Local JDK 21 + Maven (auto-installed, gitignored)
├── scripts/
│   ├── setup.sh         ← Zero-config environment setup
│   └── build.sh         ← Build + run all demo suites
│
├── modules/
│   ├── core/                          ← Aggregator for core language & runtime
│   │   ├── language/                  ← ★ Java 21 Language Features
│   │   │   ├── Step01_Records         (Algebraic data types, compact constructors)
│   │   │   ├── Step02_SealedClasses   (Tagged unions, exhaustive hierarchies)
│   │   │   ├── Step03_PatternMatching (Record patterns, nested deconstruction)
│   │   │   ├── Step04_SwitchExpr      (Expression switch, guards, null handling)
│   │   │   └── Step05_TextBlocks      (Multi-line strings, formatting)
│   │   │
│   │   ├── concurrency/               ← ★ Project Loom & Memory Model
│   │   │   ├── Step01_VirtualThreads  (M:N threading, carrier pinning)
│   │   │   ├── Step02_Primitives      (LongAdder, ReentrantLock)
│   │   │   ├── Step03_MemoryPressure  (GC allocation rates, object pooling)
│   │   │   ├── Step04_StructuredConc  (StructuredTaskScope fan-out)
│   │   │   └── Step05_ScopedValues    (ThreadLocal replacement)
│   │   │
│   │   ├── generics/                  ← Variance, recursive bounds, bridge methods
│   │   ├── metaprogramming/           ← Reflection, MethodHandles, dynamic proxies
│   │   └── streams/                   ← Advanced stream processing
│   │
│   ├── low-level/                     ← ★ Systems Programming (11 steps)
│   │   ├── Step01_MemoryLayout        (Cache lines, false sharing, @Contended)
│   │   ├── Step02_VirtualThreads      (10K concurrent requests)
│   │   ├── Step03_ForeignMemory       (Panama API, off-heap Arena)
│   │   ├── Step04_VectorAPI           (SIMD intrinsics)
│   │   ├── Step05_Observability       (JFR recording & analysis)
│   │   ├── Step06_Microbenchmarking   (JMH methodology)
│   │   ├── Step07_BinarySerialization (Manual binary IPC layout)
│   │   ├── Step08_ReferenceTypes      (Weak/Soft/Phantom references)
│   │   ├── Step09_NioSelectorEngine   (Epoll event loop)
│   │   ├── Step10_PluginSPI           (ServiceLoader architecture)
│   │   └── Step11_JMM_DeepDive        (Happens-before, volatile barriers)
│   │
│   ├── patterns/                      ← Design Patterns (Creational/Structural/Behavioral)
│   ├── functional/                    ← FP: Monads, memoization, lazy evaluation
│   ├── math-module/                   ← BigDecimal, HyperLogLog, Bloom filters
│   └── architecture/                  ← SOLID principles
│
├── docs/                              ← Reference documentation
└── notes/                             ← JVM/JDK internals notes
```

---

## 🧭 Learning Paths

### For C/C++ Engineers (shortest path)
1. **`core/language`** — Records ≈ structs, Sealed ≈ tagged unions, Pattern matching ≈ Rust match
2. **`low-level`** — Memory layout, FFM API, SIMD, NIO (the "systems" layer)
3. **`core/concurrency`** — Virtual threads, structured concurrency, JMM

### For Engineers Who Know Neither C nor Java
1. **`core/language`** — Start here for modern syntax
2. **`architecture`** — SOLID principles
3. **`patterns`** — Design patterns with real-world examples
4. **`core/concurrency`** — Concurrency from fundamentals

---

## ⚡ Running Individual Modules

```bash
# Source environment first
source .tools/env.sh

# Run a specific demo
mvn exec:exec -pl :language-module -Dexec.mainClass="com.backend.core.language.LanguageDemo"
mvn exec:exec -pl :concurrency -Dexec.mainClass="com.backend.core.concurrency.ConcurrencyDemo"
mvn exec:exec -pl :low-level -Dexec.mainClass="com.backend.lowlevel.LowLevelDemo"
```

---

## 🛠️ Engineering Standards
- [x] Java 21 LTS enforced (OpenJDK Temurin)
- [x] `--enable-preview` for Structured Concurrency & Scoped Values
- [x] Zero-friction local toolchain (no sudo, no global installs)
- [x] Every file is self-documenting with L7 annotations
- [x] C/C++ analogies in every Javadoc for cross-language engineers
