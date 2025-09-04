#!/bin/bash

# 🚀 Algorithm Practice - Professional Build Script
# This script automates the build, test, and quality check process

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_NAME="Algorithm Practice"
MAVEN_GOALS="clean compile test"
CHECKSTYLE_GOALS="checkstyle:check"
SPOTBUGS_GOALS="spotbugs:check"

echo -e "${BLUE}🚀 $PROJECT_NAME - Professional Build Process${NC}"
echo "=================================================="
echo

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Maven is available
check_maven() {
    print_status "Checking Maven installation..."
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed or not in PATH"
        print_error "Please install Maven and try again"
        exit 1
    fi
    
    MAVEN_VERSION=$(mvn -version | head -n 1)
    print_success "Maven found: $MAVEN_VERSION"
}

# Check if Java is available
check_java() {
    print_status "Checking Java installation..."
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed or not in PATH"
        print_error "Please install Java and try again"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    print_success "Java found: $JAVA_VERSION"
}

# Clean previous build artifacts
clean_build() {
    print_status "Cleaning previous build artifacts..."
    mvn clean -q
    print_success "Build artifacts cleaned"
}

# Compile the project
compile_project() {
    print_status "Compiling the project..."
    if mvn compile -q; then
        print_success "Project compiled successfully"
    else
        print_error "Compilation failed"
        exit 1
    fi
}

# Run tests
run_tests() {
    print_status "Running tests..."
    if mvn test -q; then
        print_success "All tests passed"
    else
        print_error "Some tests failed"
        exit 1
    fi
}

# Run Checkstyle
run_checkstyle() {
    print_status "Running Checkstyle code quality checks..."
    if mvn checkstyle:check -q; then
        print_success "Checkstyle passed - no code quality violations"
    else
        print_warning "Checkstyle found some code quality issues"
        print_warning "Consider fixing these for production code"
    fi
}

# Run SpotBugs
run_spotbugs() {
    print_status "Running SpotBugs bug detection..."
    if mvn spotbugs:check -q; then
        print_success "SpotBugs passed - no potential bugs detected"
    else
        print_warning "SpotBugs found potential issues"
        print_warning "Review these for potential bugs"
    fi
}

# Generate reports
generate_reports() {
    print_status "Generating project reports..."
    mvn site -q
    print_success "Project reports generated in target/site/"
}

# Run the main application
run_application() {
    print_status "Running the main application..."
    if mvn exec:java -Dexec.mainClass="com.algorithmpractice.AlgorithmPracticeApplication" -q; then
        print_success "Application ran successfully"
    else
        print_warning "Application encountered an issue"
    fi
}

# Run examples
run_examples() {
    print_status "Running algorithm examples..."
    if mvn exec:java -Dexec.mainClass="com.algorithmpractice.examples.SortingExamples" -q; then
        print_success "Examples ran successfully"
    else
        print_warning "Examples encountered an issue"
    fi
}

# Display build summary
show_summary() {
    echo
    echo -e "${GREEN}🎉 Build Process Completed Successfully!${NC}"
    echo "================================================"
    echo
    echo "✅ Project compiled successfully"
    echo "✅ All tests passed"
    echo "✅ Code quality checks completed"
    echo "✅ Bug detection completed"
    echo "✅ Reports generated"
    echo "✅ Application executed"
    echo "✅ Examples demonstrated"
    echo
    echo "📁 Build artifacts: target/"
    echo "📁 Reports: target/site/"
    echo "📁 Classes: target/classes/"
    echo
    echo "🚀 Your project is now Netflix-ready!"
}

# Main build process
main() {
    echo "Starting build process at $(date)"
    echo
    
    check_maven
    check_java
    clean_build
    compile_project
    run_tests
    run_checkstyle
    run_spotbugs
    generate_reports
    run_application
    run_examples
    show_summary
    
    echo "Build process completed at $(date)"
}

# Run the main function
main "$@"
