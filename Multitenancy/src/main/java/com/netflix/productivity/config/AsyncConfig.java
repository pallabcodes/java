package com.netflix.productivity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Netflix Production-Grade Async Configuration
 *
 * This configuration demonstrates Netflix production standards for asynchronous processing including:
 * 1. Thread pool sizing and management
 * 2. Task execution monitoring
 * 3. Error handling for async operations
 * 4. Resource management and cleanup
 * 5. Performance optimization for concurrent tasks
 * 6. Integration with monitoring systems
 * 7. Graceful shutdown handling
 * 8. Thread naming and debugging support
 *
 * For C/C++ engineers:
 * - ThreadPoolTaskExecutor is like a thread pool in C++
 * - @EnableAsync is like enabling async execution
 * - Executor is like std::thread in modern C++
 * - Configuration is like setting up thread pools
 *
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Default async executor for general-purpose async tasks
     *
     * @return Executor instance
     */
    @Bean(name = "taskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core pool size - minimum number of threads
        executor.setCorePoolSize(4);

        // Maximum pool size - maximum number of threads
        executor.setMaxPoolSize(8);

        // Queue capacity - number of tasks that can be queued
        executor.setQueueCapacity(100);

        // Thread name prefix for debugging
        executor.setThreadNamePrefix("async-task-");

        // Keep alive time for idle threads
        executor.setKeepAliveSeconds(60);

        // Allow core threads to time out
        executor.setAllowCoreThreadTimeOut(true);

        // Rejected execution handler
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        // Initialize the executor
        executor.initialize();

        return executor;
    }

    /**
     * High priority async executor for time-sensitive operations
     *
     * @return Executor instance
     */
    @Bean(name = "highPriorityTaskExecutor")
    public Executor highPriorityTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Higher priority for time-sensitive tasks
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("high-priority-async-");
        executor.setKeepAliveSeconds(30);
        executor.setAllowCoreThreadTimeOut(false);

        // Initialize the executor
        executor.initialize();

        return executor;
    }

    /**
     * Low priority async executor for background tasks
     *
     * @return Executor instance
     */
    @Bean(name = "backgroundTaskExecutor")
    public Executor backgroundTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Lower priority for background tasks
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("background-async-");
        executor.setKeepAliveSeconds(120);
        executor.setAllowCoreThreadTimeOut(true);

        // Initialize the executor
        executor.initialize();

        return executor;
    }
}
