package com.backend.designpatterns.strategy;

import java.util.List;
import java.util.Map;

/**
 * Netflix Production-Grade Strategy Pattern Interface
 * 
 * Demonstrates Netflix SDE-2 design pattern expertise:
 * - Strategy pattern interface design
 * - Generic type parameters for flexibility
 * - Production-grade method signatures
 * - Netflix-style data processing contracts
 * 
 * @author Netflix Backend Team
 * @version 1.0.0
 */
public interface DataProcessingStrategy {
    
    /**
     * Process data using the specific strategy implementation
     * 
     * @param data the input data to process
     * @return processed results as Map
     */
    Map<String, Object> processData(List<String> data);
    
    /**
     * Get strategy metadata and capabilities
     * 
     * @return strategy information
     */
    Map<String, Object> getStrategyInfo();
    
    /**
     * Validate if the strategy can handle the given data
     * 
     * @param data the data to validate
     * @return true if strategy can handle the data
     */
    boolean canHandle(List<String> data);
    
    /**
     * Get strategy performance metrics
     * 
     * @return performance metrics
     */
    Map<String, Object> getPerformanceMetrics();
    
    /**
     * Get strategy name for identification
     * 
     * @return strategy name
     */
    String getStrategyName();
}
