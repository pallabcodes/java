# Java Algorithm Project – How to Build, Run, and Test

## Prerequisites

- Java JDK 17 or higher
- Maven 3.8+
- VS Code with Java extensions (recommended)

---

## Building the Project

```bash
cd /home/pallab/Personal/algo-java
mvn clean compile
```

---

## Running All Tests

```bash
mvn test
```

---

## Running a Specific Java File (with `main` method)

### Using Maven

```bash
# Example: Run Stack.java main method
mvn exec:java -Dexec.mainClass="com.pallab.algo.array.Stack"
```

### Using Java CLI

```bash
# Compile
javac -d target/classes src/main/java/com/pallab/algo/array/Stack.java

# Run
java -cp target/classes com.pallab.algo.array.Stack
```

### Using VS Code

1. Open the file (e.g., `Stack.java`)
2. Click the "Run" or "Debug" link above the `main` method, or press `F5`

---

## Running a Specific Test Class

```bash
# Example: Run StackTest
mvn test -Dtest=com.pallab.algo.array.StackTest
```

---

## Project Structure

```
algo-java/
├── src/
│   ├── main/java/com/pallab/algo/
│   │   ├── array/
│   │   │   └── Stack.java
│   │   └── Main.java
│   └── test/java/com/pallab/algo/
│       └── array/
│           └── StackTest.java
├── pom.xml
└── .gitignore
```

---

## Common Issues

### Java Version Mismatch

Check Java version:
```bash
java --version
```
Update `pom.xml` if needed:
```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>
```

### Maven Issues

```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Update Maven
sudo pacman -Syu maven
```

---

## Adding New Algorithms

1. Create a new file in the appropriate package:
    ```bash
    touch src/main/java/com/pallab/algo/[category]/NewAlgo.java
    ```
2. Add a corresponding test:
    ```bash
    touch src/test/java/com/pallab/algo/[category]/NewAlgoTest.java
    ```

---

## Useful VS Code Shortcuts

- `F5`: Run/Debug
- `Ctrl+F5`: Run without Debug
- `Ctrl+Shift+B`: Build
- `Ctrl+Shift+P`: Command Palette

---