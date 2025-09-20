package com.netflix.springframework.demo.annotations;

import org.springframework.stereotype.Component;

/**
 * ComponentExample - Demonstrates @Component Annotation
 * 
 * @Component is the most basic Spring annotation that marks a class as a Spring component.
 * It's a generic stereotype annotation that can be used for any Spring-managed component.
 * 
 * For C/C++ engineers:
 * - @Component is like registering a class in a component registry
 * - Similar to factory pattern where Spring creates and manages instances
 * - The class becomes a "bean" that Spring can inject into other components
 * 
 * USAGE SCENARIOS:
 * - Utility classes
 * - Helper classes
 * - General-purpose components
 * - When you're not sure which specific annotation to use
 * 
 * @author Netflix SDE-2 Team
 */
@Component
public class ComponentExample {
    
    private String type = "Component";
    private String description = "Generic Spring component";
    
    /**
     * Constructor
     * 
     * Spring will call this constructor when creating the bean instance.
     * Similar to constructor in C++ but managed by Spring container.
     */
    public ComponentExample() {
        System.out.println("ComponentExample constructor called - @Component bean created");
    }
    
    /**
     * Business method
     * 
     * This method can be called by other Spring components.
     * Similar to public methods in C++ classes.
     */
    public void performOperation() {
        System.out.println("ComponentExample.performOperation() called");
        System.out.println("Type: " + type + ", Description: " + description);
    }
    
    /**
     * Getter for type
     * 
     * @return The component type
     */
    public String getType() {
        return type;
    }
    
    /**
     * Setter for type
     * 
     * @param type The component type
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * Getter for description
     * 
     * @return The component description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Setter for description
     * 
     * @param description The component description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * toString method for debugging
     * 
     * @return String representation of the component
     */
    @Override
    public String toString() {
        return "ComponentExample{" +
                "type='" + type + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
