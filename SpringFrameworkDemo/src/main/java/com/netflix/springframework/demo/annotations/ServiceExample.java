package com.netflix.springframework.demo.annotations;

import org.springframework.stereotype.Service;

/**
 * ServiceExample - Demonstrates @Service Annotation
 * 
 * @Service is a specialization of @Component that indicates the class is a service layer component.
 * It's used to mark classes that implement business logic and are part of the service layer.
 * 
 * For C/C++ engineers:
 * - @Service is like marking a class as a service in a service-oriented architecture
 * - Similar to service classes in C++ that handle business logic
 * - Spring treats @Service beans the same as @Component but with semantic meaning
 * 
 * USAGE SCENARIOS:
 * - Business logic services
 * - Application services
 * - Domain services
 * - Service layer components
 * 
 * DIFFERENCE FROM @Component:
 * - @Service is semantically more specific than @Component
 * - Both are functionally identical in Spring
 * - @Service provides better code organization and understanding
 * 
 * @author Netflix SDE-2 Team
 */
@Service
public class ServiceExample {
    
    private String type = "Service";
    private String description = "Business logic service component";
    private String businessLogic = "Complex business operations";
    
    /**
     * Constructor
     * 
     * Spring will call this constructor when creating the service bean.
     * Similar to constructor in C++ service classes.
     */
    public ServiceExample() {
        System.out.println("ServiceExample constructor called - @Service bean created");
    }
    
    /**
     * Business logic method
     * 
     * This method represents business logic that would typically be in a service layer.
     * Similar to business methods in C++ service classes.
     */
    public void executeBusinessLogic() {
        System.out.println("ServiceExample.executeBusinessLogic() called");
        System.out.println("Executing complex business operations...");
        System.out.println("Type: " + type + ", Description: " + description);
        System.out.println("Business Logic: " + businessLogic);
    }
    
    /**
     * Service method that processes data
     * 
     * This demonstrates typical service layer functionality.
     * Similar to data processing methods in C++ service classes.
     */
    public String processData(String input) {
        System.out.println("ServiceExample.processData() called with input: " + input);
        
        // Simulate business logic processing
        String processedData = "Processed: " + input.toUpperCase();
        System.out.println("Data processed: " + processedData);
        
        return processedData;
    }
    
    /**
     * Service method that validates data
     * 
     * This demonstrates validation logic typically found in service layers.
     * Similar to validation methods in C++ service classes.
     */
    public boolean validateData(String data) {
        System.out.println("ServiceExample.validateData() called with data: " + data);
        
        // Simulate validation logic
        boolean isValid = data != null && !data.trim().isEmpty();
        System.out.println("Data validation result: " + isValid);
        
        return isValid;
    }
    
    // Getters and Setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getBusinessLogic() {
        return businessLogic;
    }
    
    public void setBusinessLogic(String businessLogic) {
        this.businessLogic = businessLogic;
    }
    
    /**
     * toString method for debugging
     * 
     * @return String representation of the service
     */
    @Override
    public String toString() {
        return "ServiceExample{" +
                "type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", businessLogic='" + businessLogic + '\'' +
                '}';
    }
}
