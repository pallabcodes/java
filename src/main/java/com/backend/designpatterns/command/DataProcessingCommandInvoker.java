package com.backend.designpatterns.command;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade Command Invoker Implementation
 * 
 * Demonstrates Netflix SDE-2 design pattern expertise:
 * - Command pattern invoker implementation
 * - Command history management
 * - Undo/redo functionality
 * - Advanced HashMap operations and iterations
 * - Stream operations for command processing
 * - Optional usage for null safety
 * - Collection framework best practices
 * 
 * @author Netflix Backend Team
 * @version 1.0.0
 */
@Component
public class DataProcessingCommandInvoker {

    // Production-grade: Thread-safe collections for concurrent access
    private final Map<String, DataProcessingCommand> commandRegistry = new ConcurrentHashMap<>();
    private final List<DataProcessingCommand> commandHistory = new ArrayList<>();
    private final Map<String, Object> executionMetrics = new ConcurrentHashMap<>();
    
    // Production-grade: Command execution tracking
    private final Stack<DataProcessingCommand> undoStack = new Stack<>();
    private final Stack<DataProcessingCommand> redoStack = new Stack<>();
    
    /**
     * Register a command
     * 
     * @param command the command to register
     */
    public void registerCommand(DataProcessingCommand command) {
        // Production-grade: Command registration with HashMap
        commandRegistry.put(command.getCommandId(), command);
        executionMetrics.put("command_registered_" + command.getCommandId(), LocalDateTime.now());
    }
    
    /**
     * Execute a command by ID
     * 
     * @param commandId the command ID to execute
     * @return execution results
     */
    public Map<String, Object> executeCommand(String commandId) {
        // Production-grade: Command execution with Optional
        return Optional.ofNullable(commandRegistry.get(commandId))
                .map(this::executeCommand)
                .orElse(createErrorResult("Command not found: " + commandId));
    }
    
    /**
     * Execute a command directly
     * 
     * @param command the command to execute
     * @return execution results
     */
    public Map<String, Object> executeCommand(DataProcessingCommand command) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // Production-grade: Command validation
            Map<String, Object> validation = command.validate();
            if (!(Boolean) validation.get("valid")) {
                return createErrorResult("Command validation failed: " + validation.get("errors"));
            }
            
            // Execute command
            result = command.execute();
            result.put("commandId", command.getCommandId());
            result.put("executionTime", System.currentTimeMillis() - startTime);
            result.put("timestamp", LocalDateTime.now());
            result.put("status", "SUCCESS");
            
            // Update command history using HashMap operations
            commandHistory.add(command);
            updateExecutionMetrics(command, System.currentTimeMillis() - startTime, true);
            
            // Push to undo stack if command can be undone
            if (command.canUndo()) {
                undoStack.push(command);
                // Clear redo stack when new command is executed
                redoStack.clear();
            }
            
        } catch (Exception e) {
            result = createErrorResult("Command execution failed: " + e.getMessage());
            updateExecutionMetrics(command, System.currentTimeMillis() - startTime, false);
        }
        
        return result;
    }
    
    /**
     * Undo last command
     * 
     * @return undo results
     */
    public Map<String, Object> undoLastCommand() {
        if (undoStack.isEmpty()) {
            return createErrorResult("No commands to undo");
        }
        
        DataProcessingCommand command = undoStack.pop();
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Execute undo operation
            result = command.undo();
            result.put("commandId", command.getCommandId());
            result.put("operation", "UNDO");
            result.put("timestamp", LocalDateTime.now());
            result.put("status", "SUCCESS");
            
            // Push to redo stack
            redoStack.push(command);
            
            // Update metrics
            updateExecutionMetrics(command, 0L, true);
            
        } catch (Exception e) {
            result = createErrorResult("Undo operation failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Redo last undone command
     * 
     * @return redo results
     */
    public Map<String, Object> redoLastCommand() {
        if (redoStack.isEmpty()) {
            return createErrorResult("No commands to redo");
        }
        
        DataProcessingCommand command = redoStack.pop();
        return executeCommand(command);
    }
    
    /**
     * Get command history using HashMap operations
     * 
     * @return command history
     */
    public Map<String, Object> getCommandHistory() {
        Map<String, Object> history = new HashMap<>();
        
        // Production-grade: History analysis using Streams
        history.put("totalCommands", commandHistory.size());
        history.put("undoStackSize", undoStack.size());
        history.put("redoStackSize", redoStack.size());
        
        // Method 1: Using Streams for command analysis
        Map<String, Long> commandTypeCounts = commandHistory.stream()
                .collect(Collectors.groupingBy(
                    DataProcessingCommand::getCommandId,
                    Collectors.counting()
                ));
        history.put("commandTypeCounts", commandTypeCounts);
        
        // Method 2: Using forEach with lambda for detailed analysis
        Map<String, Object> commandDetails = new HashMap<>();
        commandHistory.forEach(command -> {
            String commandId = command.getCommandId();
            if (!commandDetails.containsKey(commandId)) {
                Map<String, Object> details = new HashMap<>();
                details.put("executionCount", 0L);
                details.put("canUndo", command.canUndo());
                details.put("lastExecuted", LocalDateTime.now());
                commandDetails.put(commandId, details);
            }
            
            Map<String, Object> details = (Map<String, Object>) commandDetails.get(commandId);
            details.put("executionCount", (Long) details.get("executionCount") + 1);
        });
        history.put("commandDetails", commandDetails);
        
        return history;
    }
    
    /**
     * Get execution metrics using HashMap operations
     * 
     * @return execution metrics
     */
    public Map<String, Object> getExecutionMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Production-grade: Metrics aggregation using Streams
        metrics.put("totalRegisteredCommands", commandRegistry.size());
        metrics.put("totalExecutedCommands", commandHistory.size());
        metrics.put("undoStackSize", undoStack.size());
        metrics.put("redoStackSize", redoStack.size());
        
        // Method 1: Using entrySet iteration for metrics analysis
        Map<String, Object> commandMetrics = new HashMap<>();
        for (Map.Entry<String, Object> entry : executionMetrics.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("execution_time_")) {
                commandMetrics.put(key, entry.getValue());
            }
        }
        metrics.put("executionTimes", commandMetrics);
        
        // Method 2: Using Streams for metrics filtering
        Map<String, Object> successMetrics = executionMetrics.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("success_"))
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue
                ));
        metrics.put("successMetrics", successMetrics);
        
        return metrics;
    }
    
    /**
     * Clear command history
     */
    public void clearHistory() {
        commandHistory.clear();
        undoStack.clear();
        redoStack.clear();
        executionMetrics.clear();
    }
    
    /**
     * Get available commands using HashMap operations
     * 
     * @return available commands
     */
    public Map<String, Object> getAvailableCommands() {
        Map<String, Object> availableCommands = new HashMap<>();
        
        // Production-grade: Command information using HashMap iteration
        commandRegistry.forEach((commandId, command) -> {
            Map<String, Object> commandInfo = new HashMap<>();
            commandInfo.put("canUndo", command.canUndo());
            commandInfo.put("commandInfo", command.getCommandInfo());
            availableCommands.put(commandId, commandInfo);
        });
        
        return availableCommands;
    }
    
    /**
     * Update execution metrics using HashMap operations
     * 
     * @param command the executed command
     * @param executionTime execution time in milliseconds
     * @param success whether execution was successful
     */
    private void updateExecutionMetrics(DataProcessingCommand command, long executionTime, boolean success) {
        String commandId = command.getCommandId();
        
        // Production-grade: Metrics tracking with HashMap
        executionMetrics.put("execution_time_" + commandId, executionTime);
        executionMetrics.put("success_" + commandId, success);
        executionMetrics.put("last_execution_" + commandId, LocalDateTime.now());
        
        // Update success count
        String successKey = "success_count_" + commandId;
        Long currentCount = (Long) executionMetrics.getOrDefault(successKey, 0L);
        executionMetrics.put(successKey, currentCount + (success ? 1 : 0));
    }
    
    /**
     * Create error result using HashMap
     * 
     * @param errorMessage the error message
     * @return error result map
     */
    private Map<String, Object> createErrorResult(String errorMessage) {
        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("status", "ERROR");
        errorResult.put("error", errorMessage);
        errorResult.put("timestamp", LocalDateTime.now());
        return errorResult;
    }
}
