package com.backend.controller;

import com.backend.service.DataProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Data Controller demonstrating collection framework usage
 * 
 * Shows Netflix SDE-2 production-grade patterns:
 * - HashMap operations and iterations
 * - Optional usage patterns
 * - Stream operations
 * - Collection framework best practices
 */
@RestController
@RequestMapping("/data")
public class DataController {

    private final DataProcessingService dataProcessingService;

    @Autowired
    public DataController(DataProcessingService dataProcessingService) {
        this.dataProcessingService = dataProcessingService;
    }

    /**
     * Process data with collections and return results
     * 
     * @param data list of data to process
     * @return processed results
     */
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processData(@RequestBody List<String> data) {
        Map<String, Object> results = dataProcessingService.processDataWithCollections(data);
        return ResponseEntity.ok(results);
    }

    /**
     * Get processed data using Optional patterns
     * 
     * @param key the key to retrieve
     * @return processed data or empty response
     */
    @GetMapping("/processed/{key}")
    public ResponseEntity<Map<String, Object>> getProcessedData(@PathVariable String key) {
        return dataProcessingService.getProcessedDataWithOptional(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Demonstrate HashMap iteration patterns
     * 
     * @return HashMap iteration examples
     */
    @GetMapping("/iterations")
    public ResponseEntity<Map<String, String>> demonstrateIterations() {
        Map<String, String> examples = dataProcessingService.demonstrateHashMapIterations();
        return ResponseEntity.ok(examples);
    }

    /**
     * Demonstrate advanced collection operations
     * 
     * @return advanced collection operations
     */
    @GetMapping("/collections")
    public ResponseEntity<Map<String, Object>> demonstrateCollections() {
        Map<String, Object> results = dataProcessingService.demonstrateAdvancedCollections();
        return ResponseEntity.ok(results);
    }

    /**
     * Process queue with collections
     * 
     * @param queueName name of the queue
     * @param items items to add
     * @return queue status
     */
    @PostMapping("/queue/{queueName}")
    public ResponseEntity<Map<String, Object>> processQueue(
            @PathVariable String queueName,
            @RequestBody List<String> items) {
        Map<String, Object> result = dataProcessingService.processQueueWithCollections(queueName, items);
        return ResponseEntity.ok(result);
    }

    /**
     * Process set with collections
     * 
     * @param setKey key for the set
     * @param values values to add
     * @return set operations result
     */
    @PostMapping("/set/{setKey}")
    public ResponseEntity<Map<String, Object>> processSet(
            @PathVariable String setKey,
            @RequestBody List<String> values) {
        Map<String, Object> result = dataProcessingService.processSetWithCollections(setKey, values);
        return ResponseEntity.ok(result);
    }

    /**
     * Quick test endpoint with sample data
     * 
     * @return sample processing results
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        List<String> sampleData = Arrays.asList("Hello", "World", "Netflix", "Backend", "Engineering");
        Map<String, Object> results = dataProcessingService.processDataWithCollections(sampleData);
        return ResponseEntity.ok(results);
    }
}
