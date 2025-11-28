package org.example.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    @Autowired
    private MeterRegistry meterRegistry;

    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core pool size - minimum number of threads
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());

        // Max pool size - maximum number of threads
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);

        // Queue capacity - tasks waiting in queue
        executor.setQueueCapacity(500);

        // Thread name prefix for easier debugging
        executor.setThreadNamePrefix("producer-consumer-async-");

        // Keep alive time for idle threads
        executor.setKeepAliveSeconds(60);

        // Rejection policy - what to do when queue is full
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        // Allow core threads to timeout
        executor.setAllowCoreThreadTimeOut(true);

        // Initialize the executor
        executor.initialize();

        // Add metrics monitoring
        ExecutorServiceMetrics.monitor(meterRegistry, executor.getThreadPoolExecutor(), "async.executor");

        logger.info("Initialized async executor with core pool size: {}, max pool size: {}, queue capacity: {}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }

    @Bean(name = "kafkaExecutor")
    public ThreadPoolTaskExecutor kafkaExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Kafka-specific optimizations
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("kafka-consumer-");
        executor.setKeepAliveSeconds(300);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.setAllowCoreThreadTimeOut(false);

        executor.initialize();

        // Add metrics monitoring
        ExecutorServiceMetrics.monitor(meterRegistry, executor.getThreadPoolExecutor(), "kafka.executor");

        return executor;
    }

    @Bean(name = "ioExecutor")
    public ThreadPoolTaskExecutor ioExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // IO-bound operations
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("io-operation-");
        executor.setKeepAliveSeconds(120);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(45);
        executor.setAllowCoreThreadTimeOut(true);

        executor.initialize();

        // Add metrics monitoring
        ExecutorServiceMetrics.monitor(meterRegistry, executor.getThreadPoolExecutor(), "io.executor");

        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return taskExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }

    private static class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        private static final Logger logger = LoggerFactory.getLogger(CustomAsyncExceptionHandler.class);

        @Override
        public void handleUncaughtException(Throwable ex, java.lang.reflect.Method method, Object... params) {
            logger.error("Uncaught async exception in method: {} with params: {}",
                    method.getName(), java.util.Arrays.toString(params), ex);

            // Could integrate with error reporting service here
            // Example: errorReportingService.reportAsyncError(ex, method, params);
        }
    }
}