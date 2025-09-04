package com.backend.controller;

import com.backend.designpatterns.builder.DataProcessingRequest;
import com.backend.designpatterns.command.DataProcessingCommandInvoker;
import com.backend.designpatterns.factory.DataProcessorFactory;
import com.backend.designpatterns.observer.DataProcessingSubject;
import com.backend.designpatterns.strategy.DataProcessingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Netflix Production-Grade Design Patterns Controller
 * 
 * Demonstrates Netflix SDE-2 design pattern expertise:
 * - Factory Pattern with strategy creation
 * - Strategy Pattern with multiple implementations
 * - Observer Pattern with event management
 * - Builder Pattern with fluent interface
 * - Command Pattern with undo/redo functionality
 * - Advanced HashMap operations and iterations
 * - Stream operations and Optional usage
 * - Collection framework best practices
 * 
 * @author Netflix Backend Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/design-patterns")
public class DesignPatternsController {

    private final DataProcessorFactory dataProcessorFactory;
    private final DataProcessingSubject dataProcessingSubject;
    private final DataProcessingCommandInvoker commandInvoker;

    @Autowired
    public DesignPatternsController(
            DataProcessorFactory dataProcessorFactory,
            DataProcessingSubject dataProcessingSubject,
            DataProcessingCommandInvoker commandInvoker) {
        this.dataProcessorFactory = dataProcessorFactory;
        this.dataProcessingSubject = dataProcessingSubject;
        this.commandInvoker = commandInvoker;
    }

    /**
     * Demonstrate Factory Pattern with strategy creation
     * 
     * @return factory pattern demonstration
     */
    @GetMapping("/factory")
    public ResponseEntity<Map<String, Object>> demonstrateFactoryPattern() {
        Map<String, Object> result = new java.util.HashMap<>();
        
        // Production-grade: Factory pattern usage
        result.put("availableStrategies", dataProcessorFactory.getAvailableStrategies());
        result.put("strategyHealthStatus", dataProcessorFactory.getStrategyHealthStatus());
        
        // Create strategies using factory
        java.util.Optional<DataProcessingStrategy> batchStrategy = 
            dataProcessorFactory.createStrategy("BATCH");
        java.util.Optional<DataProcessingStrategy> realTimeStrategy = 
            dataProcessorFactory.createStrategy("REAL_TIME");
        
        result.put("batchStrategyCreated", batchStrategy.isPresent());
        result.put("realTimeStrategyCreated", realTimeStrategy.isPresent());
        
        // Demonstrate strategy info
        batchStrategy.ifPresent(strategy -> 
            result.put("batchStrategyInfo", strategy.getStrategyInfo()));
        
        return ResponseEntity.ok(result);
    }

    /**
     * Demonstrate Strategy Pattern with data processing
     * 
     * @param data input data to process
     * @return strategy pattern demonstration
     */
    @PostMapping("/strategy")
    public ResponseEntity<Map<String, Object>> demonstrateStrategyPattern(
            @RequestBody List<String> data) {
        
        Map<String, Object> result = new java.util.HashMap<>();
        
        // Production-grade: Strategy pattern usage with HashMap operations
        java.util.Map<String, DataProcessingStrategy> strategies = new java.util.HashMap<>();
        
        // Get strategies using factory
        dataProcessorFactory.createStrategy("BATCH").ifPresent(strategy -> 
            strategies.put("BATCH", strategy));
        dataProcessorFactory.createStrategy("REAL_TIME").ifPresent(strategy -> 
            strategies.put("REAL_TIME", strategy));
        
        // Process data with different strategies using HashMap iteration
        java.util.Map<String, Object> strategyResults = new java.util.HashMap<>();
        
        // Method 1: Using entrySet iteration
        for (java.util.Map.Entry<String, DataProcessingStrategy> entry : strategies.entrySet()) {
            String strategyType = entry.getKey();
            DataProcessingStrategy strategy = entry.getValue();
            
            if (strategy.canHandle(data)) {
                strategyResults.put(strategyType, strategy.processData(data));
            } else {
                strategyResults.put(strategyType, "Cannot handle this data");
            }
        }
        
        // Method 2: Using forEach with lambda
        strategies.forEach((strategyType, strategy) -> {
            if (!strategyResults.containsKey(strategyType)) {
                strategyResults.put(strategyType + "_info", strategy.getStrategyInfo());
            }
        });
        
        result.put("strategies", strategyResults);
        result.put("inputDataSize", data.size());
        result.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(result);
    }

    /**
     * Demonstrate Observer Pattern with event management
     * 
     * @return observer pattern demonstration
     */
    @GetMapping("/observer")
    public ResponseEntity<Map<String, Object>> demonstrateObserverPattern() {
        Map<String, Object> result = new java.util.HashMap<>();
        
        // Production-grade: Observer pattern usage
        result.put("observerStatistics", dataProcessingSubject.getObserverStatistics());
        result.put("eventHistory", dataProcessingSubject.getEventHistory());
        
        // Trigger some events to demonstrate observer pattern
        java.util.Map<String, Object> eventData = new java.util.HashMap<>();
        eventData.put("message", "Design patterns demonstration");
        eventData.put("level", "INFO");
        
        dataProcessingSubject.notifyObservers("DESIGN_PATTERNS_DEMO", eventData);
        
        result.put("eventTriggered", "DESIGN_PATTERNS_DEMO");
        result.put("eventData", eventData);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Demonstrate Builder Pattern with request creation
     * 
     * @param data input data for request
     * @return builder pattern demonstration
     */
    @PostMapping("/builder")
    public ResponseEntity<Map<String, Object>> demonstrateBuilderPattern(
            @RequestBody List<String> data) {
        
        try {
            // Production-grade: Builder pattern usage with HashMap operations
            DataProcessingRequest request = DataProcessingRequest.builder()
                    .requestId("REQ_" + System.currentTimeMillis())
                    .strategyType("BATCH")
                    .data(data)
                    .parameter("priority", "HIGH")
                    .parameter("async", true)
                    .priority("HIGH")
                    .async(true)
                    .build();
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("requestCreated", true);
            result.put("requestSummary", request.getSummary());
            result.put("validation", request.validate());
            
            // Demonstrate HashMap operations from the request
            result.put("parameterCount", request.getParameters().size());
            result.put("dataSize", request.getData().size());
            
            // Method 1: Using entrySet iteration
            java.util.Map<String, Object> paramDetails = new java.util.HashMap<>();
            for (java.util.Map.Entry<String, Object> entry : request.getParameters().entrySet()) {
                paramDetails.put(entry.getKey(), entry.getValue());
            }
            result.put("parameterDetails", paramDetails);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> errorResult = new java.util.HashMap<>();
            errorResult.put("error", "Builder pattern failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResult);
        }
    }

    /**
     * Demonstrate Command Pattern with execution and undo
     * 
     * @return command pattern demonstration
     */
    @GetMapping("/command")
    public ResponseEntity<Map<String, Object>> demonstrateCommandPattern() {
        Map<String, Object> result = new java.util.HashMap<>();
        
        // Production-grade: Command pattern usage
        result.put("availableCommands", commandInvoker.getAvailableCommands());
        result.put("commandHistory", commandInvoker.getCommandHistory());
        result.put("executionMetrics", commandInvoker.getExecutionMetrics());
        
        return ResponseEntity.ok(result);
    }

    /**
     * Demonstrate all design patterns together
     * 
     * @return comprehensive design patterns demonstration
     */
    @GetMapping("/comprehensive")
    public ResponseEntity<Map<String, Object>> demonstrateAllPatterns() {
        Map<String, Object> result = new java.util.HashMap<>();
        
        // Production-grade: Comprehensive pattern demonstration
        result.put("factoryPattern", demonstrateFactoryPattern().getBody());
        result.put("observerPattern", demonstrateObserverPattern().getBody());
        result.put("commandPattern", demonstrateCommandPattern().getBody());
        
        // Create sample data for strategy demonstration
        List<String> sampleData = Arrays.asList(
            "Netflix", "Production", "Grade", "Design", "Patterns",
            "HashMap", "Operations", "Stream", "Operations", "Optional", "Usage"
        );
        
        result.put("strategyPattern", demonstrateStrategyPattern(sampleData).getBody());
        
        // Create sample request for builder demonstration
        result.put("builderPattern", demonstrateBuilderPattern(sampleData).getBody());
        
        result.put("timestamp", java.time.LocalDateTime.now());
        result.put("patternsDemonstrated", Arrays.asList(
            "Factory", "Strategy", "Observer", "Builder", "Command"
        ));
        
        return ResponseEntity.ok(result);
    }

    /**
     * Test endpoint for quick validation
     * 
     * @return quick test results
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("status", "Design Patterns Controller is working!");
        result.put("timestamp", java.time.LocalDateTime.now());
        result.put("patterns", Arrays.asList("Factory", "Strategy", "Observer", "Builder", "Command"));
        
        return ResponseEntity.ok(result);
    }
}
