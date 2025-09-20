#!/bin/bash

# Netflix Spring Framework Demo Runner
# This script demonstrates Spring concepts for C/C++ engineers

echo "=== Netflix Spring Framework Demo ==="
echo "Demonstrating Spring concepts for C/C++ engineers"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed. Please install Maven 3.6 or higher."
    exit 1
fi

echo "Java version:"
java -version
echo ""

echo "Maven version:"
mvn -version
echo ""

echo "Building the project..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo "Build successful!"
    echo ""
    
    echo "Running Spring Framework Demo with Web Server..."
    echo "================================================"
    echo "This will start the Spring Boot application with embedded web server"
    echo "The web server will run on http://localhost:8080"
    echo "Press Ctrl+C to stop the server"
    echo ""
    
    # Start the Spring Boot application with web server
    mvn spring-boot:run &
    SPRING_PID=$!
    
    # Wait a moment for the server to start
    sleep 10
    
    echo ""
    echo "Web server is running! You can now test the API endpoints:"
    echo "  - GET    http://localhost:8080/api/v1/users"
    echo "  - GET    http://localhost:8080/api/v1/users/1"
    echo "  - POST   http://localhost:8080/api/v1/users"
    echo "  - PUT    http://localhost:8080/api/v1/users/1"
    echo "  - DELETE http://localhost:8080/api/v1/users/1"
    echo "  - GET    http://localhost:8080/api/v1/users/search?name=John"
    echo "  - GET    http://localhost:8080/api/v1/annotations/get-example"
    echo "  - GET    http://localhost:8080/actuator/health"
    echo ""
    echo "Example curl commands:"
    echo "  curl http://localhost:8080/api/v1/users"
    echo "  curl -X POST http://localhost:8080/api/v1/users -H 'Content-Type: application/json' -d '{\"name\":\"Test User\",\"email\":\"test@netflix.com\"}'"
    echo ""
    echo "Press Ctrl+C to stop the server and continue with other demos"
    
    # Wait for user to stop the server
    wait $SPRING_PID
    
    echo ""
    echo "Running Spring Comparison Demo..."
    echo "=================================="
    mvn exec:java -Dexec.mainClass="com.netflix.springframework.demo.demo.SpringComparisonDemo"
    
    echo ""
    echo "Running Spring Boot Web Demo..."
    echo "==============================="
    mvn exec:java -Dexec.mainClass="com.netflix.springframework.demo.demo.SpringBootWebDemo"
    
    echo ""
    echo "Running Tests..."
    echo "================"
    mvn test
    
    echo ""
    echo "Demo completed successfully!"
    echo "Check the output above for Spring concepts demonstration."
    echo "API documentation is available in API_DOCUMENTATION.md"
    
else
    echo "Build failed. Please check the error messages above."
    exit 1
fi
