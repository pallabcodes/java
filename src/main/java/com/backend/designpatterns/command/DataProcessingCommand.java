package com.backend.designpatterns.command;

import java.util.Map;

/**
 * Netflix Production-Grade Command Pattern Interface
 * 
 * Demonstrates Netflix SDE-2 design pattern expertise:
 * - Command pattern interface design
 * - Generic result handling
 * - Production-grade method signatures
 * - Netflix-style command execution contracts
 * 
 * @author Netflix Backend Team
 * @version 1.0.0
 */
public interface DataProcessingCommand {
    
    /**
     * Execute the command
     * 
     * @return execution results
     */
    Map<String, Object> execute();
    
    /**
     * Undo the command (if supported)
     * 
     * @return undo results
     */
    Map<String, Object> undo();
    
    /**
     * Get command identifier
     * 
     * @return command ID
     */
    String getCommandId();
    
    /**
     * Check if command can be undone
     * 
     * @return true if command can be undone
     */
    boolean canUndo();
    
    /**
     * Get command metadata
     * 
     * @return command information
     */
    Map<String, Object> getCommandInfo();
    
    /**
     * Validate command before execution
     * 
     * @return validation results
     */
    Map<String, Object> validate();
}
