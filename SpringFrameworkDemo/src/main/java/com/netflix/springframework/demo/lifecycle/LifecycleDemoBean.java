package com.netflix.springframework.demo.lifecycle;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * LifecycleDemoBean - Demonstrates Bean Lifecycle Hooks
 * 
 * This class demonstrates the complete bean lifecycle in Spring Framework.
 * 
 * For C/C++ engineers:
 * - This is similar to constructor/destructor patterns in C++
 * - Spring manages the entire lifecycle automatically
 * - Multiple lifecycle hooks provide fine-grained control
 * 
 * BEAN LIFECYCLE STAGES:
 * 1. Instantiation (Constructor)
 * 2. Dependency Injection
 * 3. @PostConstruct
 * 4. InitializingBean.afterPropertiesSet()
 * 5. Custom init method
 * 6. Bean is ready for use
 * 7. @PreDestroy
 * 8. DisposableBean.destroy()
 * 9. Custom destroy method
 * 10. Bean is destroyed
 * 
 * @author Netflix SDE-2 Team
 */
@Component
public class LifecycleDemoBean implements InitializingBean, DisposableBean {
    
    private String name;
    private boolean initialized = false;
    
    /**
     * Constructor - Called first in the lifecycle
     * 
     * Similar to constructor in C++:
     * class LifecycleDemoBean {
     * public:
     *     LifecycleDemoBean() {
     *         std::cout << "Constructor called" << std::endl;
     *     }
     * };
     */
    public LifecycleDemoBean() {
        System.out.println("1. LifecycleDemoBean Constructor called");
        this.name = "LifecycleDemoBean";
    }
    
    /**
     * @PostConstruct - Called after dependency injection
     * 
     * This is the first initialization hook called after all dependencies are injected.
     * Similar to initialization in C++ constructor after member initialization.
     * 
     * In C++, this would be like:
     * class LifecycleDemoBean {
     * public:
     *     LifecycleDemoBean() {
     *         // Constructor logic
     *         postConstruct(); // Manual call
     *     }
     * private:
     *     void postConstruct() {
     *         // Initialization logic
     *     }
     * };
     */
    @PostConstruct
    public void postConstruct() {
        System.out.println("2. @PostConstruct method called");
        System.out.println("   - All dependencies have been injected");
        System.out.println("   - Bean is ready for initialization");
    }
    
    /**
     * InitializingBean.afterPropertiesSet() - Called after @PostConstruct
     * 
     * This is the second initialization hook.
     * Similar to additional initialization in C++ constructor.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("3. InitializingBean.afterPropertiesSet() called");
        System.out.println("   - All properties have been set");
        System.out.println("   - Bean is fully initialized");
        this.initialized = true;
    }
    
    /**
     * Custom initialization method
     * 
     * This demonstrates custom initialization logic.
     * Similar to custom initialization methods in C++.
     */
    public void customInit() {
        System.out.println("4. Custom init method called");
        System.out.println("   - Custom initialization logic executed");
        System.out.println("   - Bean is ready for business operations");
    }
    
    /**
     * Business method - Called when bean is in use
     * 
     * This represents the normal operation phase of the bean.
     */
    public void performBusinessOperation() {
        System.out.println("5. Business operation called");
        System.out.println("   - Bean is fully operational");
        System.out.println("   - Name: " + name + ", Initialized: " + initialized);
    }
    
    /**
     * @PreDestroy - Called before bean destruction
     * 
     * This is the first cleanup hook called before bean destruction.
     * Similar to cleanup in C++ destructor.
     * 
     * In C++, this would be like:
     * class LifecycleDemoBean {
     * public:
     *     ~LifecycleDemoBean() {
     *         preDestroy(); // Manual call
     *         // Destructor logic
     *     }
     * private:
     *     void preDestroy() {
     *         // Cleanup logic
     *     }
     * };
     */
    @PreDestroy
    public void preDestroy() {
        System.out.println("6. @PreDestroy method called");
        System.out.println("   - Bean is about to be destroyed");
        System.out.println("   - Cleanup resources before destruction");
    }
    
    /**
     * DisposableBean.destroy() - Called after @PreDestroy
     * 
     * This is the second cleanup hook.
     * Similar to additional cleanup in C++ destructor.
     */
    @Override
    public void destroy() throws Exception {
        System.out.println("7. DisposableBean.destroy() called");
        System.out.println("   - Final cleanup operations");
        System.out.println("   - Bean is being destroyed");
    }
    
    /**
     * Custom destroy method
     * 
     * This demonstrates custom cleanup logic.
     * Similar to custom cleanup methods in C++.
     */
    public void customDestroy() {
        System.out.println("8. Custom destroy method called");
        System.out.println("   - Custom cleanup logic executed");
        System.out.println("   - Bean destruction complete");
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
}
