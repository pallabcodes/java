package com.backend.designpatterns.structural.composite;

/**
 * Step 2: COMPONENT INTERFACE
 */
public interface Step02_AccessPolicy {
    
    boolean isSatisfiedBy(Step01_UserContext context);

}
