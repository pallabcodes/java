package com.netflix.productivity.platform.jobs;

import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Enterprise Job Scheduler
 *
 * Provides comprehensive background job processing capabilities:
 * - Scheduled task execution
 * - Job queuing and prioritization
 * - Asynchronous job processing
 * - Job monitoring and metrics
 * - Failure handling and retries
 * - Job persistence and recovery
 */
@Service
public class JobScheduler {

    private static final Logger logger = LoggerFactory.getLogger(JobScheduler.class);

    // Job execution thread pool
    private final ExecutorService jobExecutor = Executors.newFixedThreadPool(10);

    // Scheduled executor for cron-like jobs
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(5);

    // Job queues by priority
    private final BlockingQueue<Job> highPriorityQueue = new PriorityBlockingQueue<>();
    private final BlockingQueue<Job> normalPriorityQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Job> lowPriorityQueue = new LinkedBlockingQueue<>();

    // Job registry and metrics
    private final ConcurrentHashMap<String, Job> activeJobs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, JobStats> jobStats = new ConcurrentHashMap<>();
    private final AtomicLong jobCounter = new AtomicLong(0);

    // Job processors
    private final JobProcessor highPriorityProcessor;
    private final JobProcessor normalPriorityProcessor;
    private final JobProcessor lowPriorityProcessor;

    public JobScheduler() {
        // Initialize job processors
        this.highPriorityProcessor = new JobProcessor("HIGH", highPriorityQueue);
        this.normalPriorityProcessor = new JobProcessor("NORMAL", normalPriorityQueue);
        this.lowPriorityProcessor = new JobProcessor("LOW", lowPriorityQueue);

        // Start job processors
        startJobProcessors();
    }

    /**
     * Submit a job for immediate execution
     */
    public String submitJob(Job job) {
        String jobId = generateJobId();
        job.setId(jobId);
        job.setSubmittedAt(LocalDateTime.now());

        activeJobs.put(jobId, job);

        // Route to appropriate queue based on priority
        switch (job.getPriority()) {
            case HIGH -> highPriorityQueue.offer(job);
            case NORMAL -> normalPriorityQueue.offer(job);
            case LOW -> lowPriorityQueue.offer(job);
        }

        logger.info("Submitted job: {} with priority: {}", jobId, job.getPriority());
        return jobId;
    }

    /**
     * Schedule a job for future execution
     */
    public String scheduleJob(Job job, long delayMillis) {
        String jobId = generateJobId();
        job.setId(jobId);
        job.setSubmittedAt(LocalDateTime.now());

        activeJobs.put(jobId, job);

        scheduledExecutor.schedule(() -> {
            logger.info("Executing scheduled job: {}", jobId);
            submitJob(job);
        }, delayMillis, TimeUnit.MILLISECONDS);

        logger.info("Scheduled job: {} for execution in {} ms", jobId, delayMillis);
        return jobId;
    }

    /**
     * Schedule a recurring job with cron-like expression
     */
    public String scheduleRecurringJob(Job job, long initialDelayMillis, long periodMillis) {
        String jobId = generateJobId();
        job.setId(jobId);
        job.setSubmittedAt(LocalDateTime.now());

        scheduledExecutor.scheduleAtFixedRate(() -> {
            logger.debug("Executing recurring job: {}", jobId);
            // Create a new instance for each execution to avoid state issues
            Job executionJob = new Job(
                job.getType(),
                job.getPayload(),
                job.getPriority(),
                job.getMaxRetries()
            );
            executionJob.setId(generateJobId() + "_exec");
            submitJob(executionJob);
        }, initialDelayMillis, periodMillis, TimeUnit.MILLISECONDS);

        logger.info("Scheduled recurring job: {} with period: {} ms", jobId, periodMillis);
        return jobId;
    }

    /**
     * Cancel a job
     */
    public boolean cancelJob(String jobId) {
        Job job = activeJobs.get(jobId);
        if (job != null) {
            job.setStatus(JobStatus.CANCELLED);
            activeJobs.remove(jobId);
            logger.info("Cancelled job: {}", jobId);
            return true;
        }
        return false;
    }

    /**
     * Get job status
     */
    public JobStatus getJobStatus(String jobId) {
        Job job = activeJobs.get(jobId);
        return job != null ? job.getStatus() : null;
    }

    /**
     * Get job details
     */
    public Job getJob(String jobId) {
        return activeJobs.get(jobId);
    }

    /**
     * Scheduled maintenance jobs
     */

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void cleanupCompletedJobs() {
        logger.debug("Running job cleanup maintenance");

        activeJobs.entrySet().removeIf(entry -> {
            Job job = entry.getValue();
            return job.getStatus() == JobStatus.COMPLETED ||
                   job.getStatus() == JobStatus.FAILED ||
                   job.getStatus() == JobStatus.CANCELLED;
        });

        logger.debug("Job cleanup completed");
    }

    @Scheduled(fixedRate = 60000) // Every minute
    public void processStuckJobs() {
        logger.debug("Processing stuck jobs");

        activeJobs.values().forEach(job -> {
            if (job.getStatus() == JobStatus.RUNNING &&
                job.getStartedAt() != null &&
                job.getStartedAt().isBefore(LocalDateTime.now().minusMinutes(30))) {

                logger.warn("Found stuck job: {}, retrying", job.getId());
                job.setStatus(JobStatus.PENDING);
                submitJob(job);
            }
        });
    }

    @Scheduled(fixedRate = 3600000) // Every hour
    public void generateJobMetrics() {
        logger.debug("Generating job metrics");

        // Update job statistics
        activeJobs.values().forEach(job -> {
            JobStats stats = jobStats.computeIfAbsent(job.getType().name(),
                k -> new JobStats(job.getType().name()));

            switch (job.getStatus()) {
                case COMPLETED -> stats.successCount++;
                case FAILED -> stats.failureCount++;
                case RUNNING -> stats.runningCount++;
                case PENDING -> stats.pendingCount++;
            }
        });
    }

    /**
     * Specific business job methods
     */

    public String scheduleIssueNotification(String issueId, String userId, long delayMillis) {
        Job notificationJob = new Job(
            JobType.ISSUE_NOTIFICATION,
            Map.of("issueId", issueId, "userId", userId),
            JobPriority.NORMAL,
            3
        );
        return scheduleJob(notificationJob, delayMillis);
    }

    public String scheduleProjectReport(String projectId) {
        Job reportJob = new Job(
            JobType.PROJECT_REPORT,
            Map.of("projectId", projectId),
            JobPriority.LOW,
            2
        );
        return scheduleRecurringJob(reportJob, 3600000, 86400000); // Start in 1 hour, repeat daily
    }

    public String scheduleDataCleanup() {
        Job cleanupJob = new Job(
            JobType.DATA_CLEANUP,
            Map.of("cleanupType", "EXPIRED_SESSIONS"),
            JobPriority.LOW,
            1
        );
        return scheduleRecurringJob(cleanupJob, 1800000, 86400000); // Start in 30 min, repeat daily
    }

    public String scheduleCacheInvalidation(String cacheKey, String pattern) {
        Job cacheJob = new Job(
            JobType.CACHE_INVALIDATION,
            Map.of("cacheKey", cacheKey, "pattern", pattern),
            JobPriority.HIGH,
            3
        );
        return submitJob(cacheJob);
    }

    /**
     * Get job scheduler statistics
     */
    public JobSchedulerStats getSchedulerStats() {
        return new JobSchedulerStats(
            activeJobs.size(),
            highPriorityQueue.size(),
            normalPriorityQueue.size(),
            lowPriorityQueue.size(),
            jobCounter.get(),
            jobStats.values().toList()
        );
    }

    /**
     * Private helper methods
     */

    private void startJobProcessors() {
        // Start high priority processor
        CompletableFuture.runAsync(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Job job = highPriorityQueue.take();
                    jobExecutor.submit(() -> processJob(job));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        // Start normal priority processor
        CompletableFuture.runAsync(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Job job = normalPriorityQueue.take();
                    jobExecutor.submit(() -> processJob(job));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        // Start low priority processor
        CompletableFuture.runAsync(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Job job = lowPriorityQueue.take();
                    jobExecutor.submit(() -> processJob(job));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        logger.info("Job processors started");
    }

    private void processJob(Job job) {
        job.setStatus(JobStatus.RUNNING);
        job.setStartedAt(LocalDateTime.now());

        try {
            logger.debug("Processing job: {} of type: {}", job.getId(), job.getType());

            // Execute job based on type
            boolean success = executeJob(job);

            if (success) {
                job.setStatus(JobStatus.COMPLETED);
                job.setCompletedAt(LocalDateTime.now());
                logger.debug("Job completed successfully: {}", job.getId());
            } else {
                handleJobFailure(job);
            }

        } catch (Exception e) {
            logger.error("Job execution failed: {}", job.getId(), e);
            handleJobFailure(job);
        }
    }

    private boolean executeJob(Job job) {
        // In production, this would route to specific job handlers
        // For simulation, we'll just simulate different execution times
        try {
            switch (job.getType()) {
                case ISSUE_NOTIFICATION -> {
                    Thread.sleep(100 + (int)(Math.random() * 200)); // 100-300ms
                    // Simulate sending notification
                    return Math.random() > 0.05; // 95% success rate
                }
                case PROJECT_REPORT -> {
                    Thread.sleep(5000 + (int)(Math.random() * 5000)); // 5-10 seconds
                    // Simulate report generation
                    return Math.random() > 0.10; // 90% success rate
                }
                case DATA_CLEANUP -> {
                    Thread.sleep(2000 + (int)(Math.random() * 3000)); // 2-5 seconds
                    // Simulate data cleanup
                    return Math.random() > 0.02; // 98% success rate
                }
                case CACHE_INVALIDATION -> {
                    Thread.sleep(50 + (int)(Math.random() * 100)); // 50-150ms
                    // Simulate cache invalidation
                    return Math.random() > 0.01; // 99% success rate
                }
                default -> {
                    Thread.sleep(100);
                    return true;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void handleJobFailure(Job job) {
        job.incrementRetryCount();

        if (job.getRetryCount() < job.getMaxRetries()) {
            // Retry the job
            job.setStatus(JobStatus.PENDING);
            logger.warn("Retrying job: {} (attempt {}/{})", job.getId(),
                       job.getRetryCount(), job.getMaxRetries());
            submitJob(job);
        } else {
            // Job failed permanently
            job.setStatus(JobStatus.FAILED);
            job.setFailedAt(LocalDateTime.now());
            logger.error("Job failed permanently: {} after {} retries",
                        job.getId(), job.getMaxRetries());
        }
    }

    private String generateJobId() {
        return "JOB_" + jobCounter.incrementAndGet() + "_" + System.currentTimeMillis();
    }

    /**
     * Shutdown the job scheduler
     */
    public void shutdown() {
        logger.info("Shutting down job scheduler");

        scheduledExecutor.shutdown();
        jobExecutor.shutdown();

        try {
            if (!scheduledExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
            if (!jobExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                jobExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduledExecutor.shutdownNow();
            jobExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        logger.info("Job scheduler shutdown complete");
    }

    /**
     * Job Processor inner class
     */
    private static class JobProcessor {
        private final String name;
        private final BlockingQueue<Job> queue;

        public JobProcessor(String name, BlockingQueue<Job> queue) {
            this.name = name;
            this.queue = queue;
        }
    }
}

/**
 * Data classes for job scheduling
 */

class Job implements Comparable<Job> {
    private String id;
    private final JobType type;
    private final Map<String, Object> payload;
    private final JobPriority priority;
    private final int maxRetries;
    private JobStatus status;
    private int retryCount;
    private LocalDateTime submittedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;

    public Job(JobType type, Map<String, Object> payload, JobPriority priority, int maxRetries) {
        this.type = type;
        this.payload = payload;
        this.priority = priority;
        this.maxRetries = maxRetries;
        this.status = JobStatus.PENDING;
        this.retryCount = 0;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public JobType getType() { return type; }
    public Map<String, Object> getPayload() { return payload; }
    public JobPriority getPriority() { return priority; }
    public int getMaxRetries() { return maxRetries; }
    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }

    public int getRetryCount() { return retryCount; }
    public void incrementRetryCount() { this.retryCount++; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getFailedAt() { return failedAt; }
    public void setFailedAt(LocalDateTime failedAt) { this.failedAt = failedAt; }

    @Override
    public int compareTo(Job other) {
        // Higher priority jobs first, then by submission time
        int priorityCompare = Integer.compare(other.priority.getLevel(), this.priority.getLevel());
        if (priorityCompare != 0) {
            return priorityCompare;
        }
        return this.submittedAt.compareTo(other.submittedAt);
    }

    @Override
    public String toString() {
        return "Job{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", priority=" + priority +
                ", status=" + status +
                '}';
    }
}

enum JobType {
    ISSUE_NOTIFICATION,
    PROJECT_REPORT,
    DATA_CLEANUP,
    CACHE_INVALIDATION,
    EMAIL_DIGEST,
    BACKUP_OPERATION
}

enum JobPriority {
    LOW(1),
    NORMAL(2),
    HIGH(3);

    private final int level;

    JobPriority(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}

enum JobStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}

class JobStats {
    private final String jobType;
    private long successCount;
    private long failureCount;
    private long runningCount;
    private long pendingCount;
    private long averageExecutionTimeMs;

    public JobStats(String jobType) {
        this.jobType = jobType;
        this.successCount = 0;
        this.failureCount = 0;
        this.runningCount = 0;
        this.pendingCount = 0;
        this.averageExecutionTimeMs = 0;
    }

    // Getters and setters
    public String getJobType() { return jobType; }
    public long getSuccessCount() { return successCount; }
    public void setSuccessCount(long successCount) { this.successCount = successCount; }
    public long getFailureCount() { return failureCount; }
    public void setFailureCount(long failureCount) { this.failureCount = failureCount; }
    public long getRunningCount() { return runningCount; }
    public void setRunningCount(long runningCount) { this.runningCount = runningCount; }
    public long getPendingCount() { return pendingCount; }
    public void setPendingCount(long pendingCount) { this.pendingCount = pendingCount; }
    public long getAverageExecutionTimeMs() { return averageExecutionTimeMs; }
    public void setAverageExecutionTimeMs(long averageExecutionTimeMs) { this.averageExecutionTimeMs = averageExecutionTimeMs; }

    public long getTotalCount() {
        return successCount + failureCount + runningCount + pendingCount;
    }

    public double getSuccessRate() {
        long total = getTotalCount();
        return total > 0 ? (double) successCount / total : 0.0;
    }
}

class JobSchedulerStats {
    private final int activeJobs;
    private final int highPriorityQueueSize;
    private final int normalPriorityQueueSize;
    private final int lowPriorityQueueSize;
    private final long totalJobsProcessed;
    private final java.util.List<JobStats> jobTypeStats;

    public JobSchedulerStats(int activeJobs, int highPriorityQueueSize, int normalPriorityQueueSize,
                           int lowPriorityQueueSize, long totalJobsProcessed,
                           java.util.List<JobStats> jobTypeStats) {
        this.activeJobs = activeJobs;
        this.highPriorityQueueSize = highPriorityQueueSize;
        this.normalPriorityQueueSize = normalPriorityQueueSize;
        this.lowPriorityQueueSize = lowPriorityQueueSize;
        this.totalJobsProcessed = totalJobsProcessed;
        this.jobTypeStats = jobTypeStats;
    }

    // Getters
    public int getActiveJobs() { return activeJobs; }
    public int getHighPriorityQueueSize() { return highPriorityQueueSize; }
    public int getNormalPriorityQueueSize() { return normalPriorityQueueSize; }
    public int getLowPriorityQueueSize() { return lowPriorityQueueSize; }
    public long getTotalJobsProcessed() { return totalJobsProcessed; }
    public java.util.List<JobStats> getJobTypeStats() { return jobTypeStats; }

    public int getTotalQueuedJobs() {
        return highPriorityQueueSize + normalPriorityQueueSize + lowPriorityQueueSize;
    }
}
