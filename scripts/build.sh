#!/bin/bash

# 🚀 Java Mastery - Professional Build & Execute Script
# Uses local .tools/ environment.

set -e

WORKSPACE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "$WORKSPACE_DIR/.tools/env.sh"

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🚀 Java 21 Mastery - Build & Run${NC}"
echo "---------------------------------------------------------"

# 1. Build
echo "🏗️ Building Monorepo..."
mvn clean install -DskipTests -q

# 2. Run Language Features Demo
echo -e "\n${GREEN}▶ Running Java 21 Language Features Demo...${NC}"
mvn exec:exec -pl :language-module -Dexec.mainClass="com.backend.core.language.LanguageDemo" -q

# 3. Run Concurrency Demo
echo -e "\n${GREEN}▶ Running Concurrency Demo...${NC}"
mvn exec:exec -pl :concurrency -Dexec.mainClass="com.backend.core.concurrency.ConcurrencyDemo" -q

# 4. Run Low-Level Systems Demo
echo -e "\n${GREEN}▶ Running Low-Level Systems Demo...${NC}"
mvn exec:exec -pl :low-level -Dexec.mainClass="com.backend.lowlevel.LowLevelDemo" -q

echo -e "\n---------------------------------------------------------"
echo -e "${GREEN}✅ All Demos Completed Successfully!${NC}"
