#!/bin/bash

# 🛠️ Java Mastery - Environment Setup Script
# Installs JDK 21 and Maven 3.9+ locally in .tools/ directory.
# No sudo required.

set -e

WORKSPACE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TOOLS_DIR="$WORKSPACE_DIR/.tools"
JDK_DIR="$TOOLS_DIR/jdk21"
MAVEN_DIR="$TOOLS_DIR/maven"

# Versions
JDK_URL="https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.2%2B13/OpenJDK21U-jdk_x64_linux_hotspot_21.0.2_13.tar.gz"
MAVEN_URL="https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz"

mkdir -p "$TOOLS_DIR"

echo "---------------------------------------------------------"
echo "🚀 Setting up Local Java 21 & Maven Environment"
echo "---------------------------------------------------------"

# 1. Install JDK
if [ ! -d "$JDK_DIR" ]; then
    echo "⬇️ Downloading OpenJDK 21..."
    curl -L "$JDK_URL" -o "$TOOLS_DIR/jdk.tar.gz"
    mkdir -p "$JDK_DIR"
    tar -xzf "$TOOLS_DIR/jdk.tar.gz" -C "$JDK_DIR" --strip-components=1
    rm "$TOOLS_DIR/jdk.tar.gz"
    echo "✅ JDK 21 installed in $JDK_DIR"
else
    echo "ℹ️ JDK 21 already installed."
fi

# 2. Install Maven
if [ ! -d "$MAVEN_DIR" ]; then
    echo "⬇️ Downloading Maven 3.9.6..."
    curl -L "$MAVEN_URL" -o "$TOOLS_DIR/maven.tar.gz"
    mkdir -p "$MAVEN_DIR"
    tar -xzf "$TOOLS_DIR/maven.tar.gz" -C "$MAVEN_DIR" --strip-components=1
    rm "$TOOLS_DIR/maven.tar.gz"
    echo "✅ Maven installed in $MAVEN_DIR"
else
    echo "ℹ️ Maven already installed."
fi

# 3. Create Environment Script
cat <<EOF > "$TOOLS_DIR/env.sh"
export JAVA_HOME="$JDK_DIR"
export M2_HOME="$MAVEN_DIR"
export PATH="\$JAVA_HOME/bin:\$M2_HOME/bin:\$PATH"
EOF

chmod +x "$TOOLS_DIR/env.sh"

echo "---------------------------------------------------------"
echo "🎉 Setup Complete!"
echo "To activate the environment in your current shell, run:"
echo "source .tools/env.sh"
echo "---------------------------------------------------------"
