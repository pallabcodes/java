# Java Monorepo: Engineering Standards & Usage Guide

This document serves as the architectural reference for the Java monorepo, structured to Google L5 (Senior/Principal) engineering standards.

## 🏗️ Monorepo Architecture

The project is a **Maven Multi-Module Monorepo**. This structure ensures high cohesion (related code stays together) and low coupling (modules define clear boundaries).

### Module Hierarchy
```text
java/ (Root)
├── pom.xml (Parent POM - manages dependencies like Lombok, Java 21)
├── modules/
│   ├── core/ (Aggregator for core libraries)
│   │   ├── generics/ (Advanced Generic patterns, Type Erasure handling)
│   │   ├── metaprogramming/ (Reflection, Annotations)
│   │   └── streams/ (Advanced Stream implementation with Circuit Breakers)
│   ├── design-patterns/ (SOLID, OOP, Creational/Behavioral patterns)
│   ├── functional/ (Functional programming & Monadic patterns)
│   └── netflix-math/ (High-performance mathematical utilities)
```

---

## 🚀 Build & Execution Guide

### 1. Environment Requirements
The project uses **Java 21** features (Records, Pattern Matching, Advanced Type Inference).
```zsh
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

### 2. Standard Build Commands
Build the entire monorepo and install modules to local repo:
```bash
mvn clean install -DskipTests
```

### 3. Running Specific Tests
Run all tests for the streams module:
```bash
mvn test -pl :streams-module
```

Run a single specific test class:
```bash
mvn test -Dtest=AdvancedStreamProcessorTest
```

---

## 🛠️ Key Engineering Achievements

### 1. Advanced Stream Pattern (`AdvancedStream`)
Unlike standard Java Streams, our `AdvancedStream` implementation includes:
*   **Circuit Breaker Protection**: Automatic failure detection and fallback strategies.
*   **Backpressure Handling**: Rate limiting and semaphore-based flow control.
*   **Adaptive Processing**: Strategies that switch between Sequential and Parallel based on data size.

### 2. Type Inference Stability
We solved the complex "Diamond Operator" inference failure in anonymous inner classes by refactoring the implementation to use eager collection into typed containers, ensuring compatibility with the strict Java 21 compiler.

### 3. Unified Tooling
*   **Lombok**: Global configuration in the root POM for boilerplate-free POJOs.
*   **SLF4J/Logback**: Standardized logging across all modules, avoiding common `@Slf4j` pitfalls in static utility classes and private constructors.

---

## 🧑‍💻 L5 Implementation Checklist
- [x] Java 21 Enforced
- [x] Multi-module isolation
- [x] Type-safe custom collectors
- [x] Thread-safe processing strategies
- [x] Dependency management via Root POM
