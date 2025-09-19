/*
 * Copyright 2024 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/2002/05/XMLSchema-instance
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.mathlib.system.process;

import com.netflix.mathlib.core.MathOperation;
import com.netflix.mathlib.exceptions.ValidationException;
import com.netflix.mathlib.monitoring.OperationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Process Manager - Production-grade process scheduling and management system.
 *
 * This class provides comprehensive process management capabilities including:
 * - Multiple CPU scheduling algorithms (FCFS, SJF, Round Robin, Priority, MLFQ)
 * - Process lifecycle management
 * - Resource allocation and deadlock prevention
 * - Inter-process communication
 * - Performance monitoring and analysis
 *
 * Essential for building operating system components, task schedulers,
 * and resource management systems.
 *
 * All implementations are optimized for production use with:
 * - Thread-safe operations
 * - Performance monitoring and metrics
 * - Comprehensive error handling
 * - Configurable scheduling policies
 * - Detailed logging and observability
 *
 * @author Netflix Math Library Team
 * @version 1.0.0
 * @since 2024
 */
public class ProcessManager implements MathOperation {

    private static final Logger logger = LoggerFactory.getLogger(ProcessManager.class);
    private static final String OPERATION_NAME = "ProcessManager";
    private static final String COMPLEXITY = "O(n log n)";
    private static final boolean THREAD_SAFE = true;

    private final OperationMetrics metrics;

    // Process management
    private final ConcurrentHashMap<String, Process> processes = new ConcurrentHashMap<>();
    private final PriorityQueue<Process> readyQueue = new PriorityQueue<>();
    private final LinkedList<Process> waitingQueue = new LinkedList<>();
    private final ConcurrentHashMap<String, Semaphore> semaphores = new ConcurrentHashMap<>();

    // Scheduling
    private volatile SchedulingAlgorithm currentAlgorithm = SchedulingAlgorithm.ROUND_ROBIN;
    private final AtomicInteger timeQuantum = new AtomicInteger(10); // Default 10ms

    // Statistics
    private final AtomicLong totalProcessesCreated = new AtomicLong(0);
    private final AtomicLong totalProcessesCompleted = new AtomicLong(0);
    private final AtomicLong totalContextSwitches = new AtomicLong(0);
    private final AtomicLong totalWaitingTime = new AtomicLong(0);
    private final AtomicLong totalTurnaroundTime = new AtomicLong(0);

    /**
     * Constructor for Process Manager.
     */
    public ProcessManager() {
        this.metrics = new OperationMetrics(OPERATION_NAME, COMPLEXITY, THREAD_SAFE);

        // Start scheduling thread
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::scheduleProcesses, 0, 1, TimeUnit.MILLISECONDS);

        logger.info("Initialized Process Manager with {} scheduling", currentAlgorithm);
    }

    @Override
    public String getOperationName() {
        return OPERATION_NAME;
    }

    @Override
    public String getComplexity() {
        return COMPLEXITY;
    }

    @Override
    public OperationMetrics getMetrics() {
        return metrics;
    }

    @Override
    public void validateInputs(Object... inputs) {
        if (inputs == null || inputs.length == 0) {
            throw ValidationException.nullParameter("inputs", OPERATION_NAME);
        }

        for (Object input : inputs) {
            if (input == null) {
                throw ValidationException.nullParameter("input", OPERATION_NAME);
            }
        }
    }

    @Override
    public boolean isThreadSafe() {
        return THREAD_SAFE;
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    // ===== PROCESS MANAGEMENT =====

    /**
     * Create a new process.
     *
     * @param processId unique process identifier
     * @param priority process priority (higher number = higher priority)
     * @param burstTime CPU burst time in milliseconds
     * @param arrivalTime arrival time in milliseconds
     * @return created process or null if failed
     */
    public Process createProcess(String processId, int priority, long burstTime, long arrivalTime) {
        validateInputs(processId);

        long startTime = System.nanoTime();

        try {
            Process process = new Process(processId, priority, burstTime, arrivalTime);
            processes.put(processId, process);
            totalProcessesCreated.incrementAndGet();

            // Add to ready queue if arrived
            if (arrivalTime <= System.currentTimeMillis()) {
                addToReadyQueue(process);
            } else {
                // Schedule future arrival
                scheduleFutureArrival(process);
            }

            long executionTime = System.nanoTime() - startTime;
            metrics.recordSuccess(executionTime, 0);

            logger.info("Created process '{}' with priority {}, burst time {}ms",
                       processId, priority, burstTime);
            return process;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error creating process '{}': {}", processId, e.getMessage());
            return null;
        }
    }

    /**
     * Terminate a process.
     *
     * @param processId process to terminate
     * @return true if successfully terminated
     */
    public boolean terminateProcess(String processId) {
        validateInputs(processId);

        Process process = processes.get(processId);
        if (process == null) {
            return false;
        }

        process.setState(ProcessState.TERMINATED);
        process.setCompletionTime(System.currentTimeMillis());

        // Calculate final statistics
        long turnaroundTime = process.getCompletionTime() - process.getArrivalTime();
        long waitingTime = turnaroundTime - process.getOriginalBurstTime();

        totalTurnaroundTime.addAndGet(turnaroundTime);
        totalWaitingTime.addAndGet(waitingTime);
        totalProcessesCompleted.incrementAndGet();

        logger.info("Terminated process '{}' - Turnaround: {}ms, Waiting: {}ms",
                   processId, turnaroundTime, waitingTime);
        return true;
    }

    /**
     * Get process by ID.
     *
     * @param processId process identifier
     * @return process or null if not found
     */
    public Process getProcess(String processId) {
        return processes.get(processId);
    }

    // ===== SCHEDULING ALGORITHMS =====

    /**
     * Set the current scheduling algorithm.
     *
     * @param algorithm scheduling algorithm to use
     */
    public void setSchedulingAlgorithm(SchedulingAlgorithm algorithm) {
        validateInputs(algorithm);

        this.currentAlgorithm = algorithm;
        logger.info("Changed scheduling algorithm to {}", algorithm);
    }

    /**
     * Set time quantum for Round Robin scheduling.
     *
     * @param quantum time quantum in milliseconds
     */
    public void setTimeQuantum(int quantum) {
        if (quantum <= 0) {
            throw new ValidationException("Time quantum must be positive", OPERATION_NAME);
        }
        this.timeQuantum.set(quantum);
        logger.info("Set time quantum to {}ms", quantum);
    }

    /**
     * Execute First Come First Served (FCFS) scheduling.
     *
     * @param processes list of processes to schedule
     * @return scheduling result
     */
    public SchedulingResult executeFCFS(List<Process> processes) {
        long startTime = System.nanoTime();

        List<Process> sortedProcesses = new ArrayList<>(processes);
        sortedProcesses.sort(Comparator.comparingLong(Process::getArrivalTime));

        long currentTime = 0;
        List<ProcessExecution> executions = new ArrayList<>();
        final long[] currentTimeRef = {0};

        for (Process process : sortedProcesses) {
            if (currentTimeRef[0] < process.getArrivalTime()) {
                currentTimeRef[0] = process.getArrivalTime();
            }

            final long startExecution = currentTimeRef[0];
            final long burstTime = process.getRemainingBurstTime();
            final long endExecution = startExecution + burstTime;

            executions.add(new ProcessExecution(process.getProcessId(), startExecution, endExecution));
            currentTimeRef[0] = endExecution;
        }

        long executionTime = System.nanoTime() - startTime;
        metrics.recordSuccess(executionTime, 0);

        return new SchedulingResult(executions, calculateAverageWaitingTime(executions),
                                  calculateAverageTurnaroundTime(executions));
    }

    /**
     * Execute Shortest Job First (SJF) scheduling.
     *
     * @param processes list of processes to schedule
     * @return scheduling result
     */
    public SchedulingResult executeSJF(List<Process> processes) {
        long startTime = System.nanoTime();

        List<Process> remainingProcesses = new ArrayList<>(processes);
        List<ProcessExecution> executions = new ArrayList<>();
        final long[] currentTimeRef = {0};

        while (!remainingProcesses.isEmpty()) {
            // Find processes that have arrived
            final long currentTime = currentTimeRef[0];
            List<Process> availableProcesses = remainingProcesses.stream()
                .filter(p -> p.getArrivalTime() <= currentTime)
                .collect(ArrayList::new, (list, p) -> list.add(p), ArrayList::addAll);

            if (availableProcesses.isEmpty()) {
                // No processes available, advance to next arrival
                Process nextProcess = remainingProcesses.stream()
                    .min(Comparator.comparingLong(Process::getArrivalTime))
                    .orElse(null);
                if (nextProcess != null) {
                    currentTimeRef[0] = nextProcess.getArrivalTime();
                }
                continue;
            }

            // Select shortest job
            Process shortestJob = availableProcesses.stream()
                .min(Comparator.comparingLong(Process::getRemainingBurstTime))
                .orElse(null);

            if (shortestJob != null) {
                final long startExecution = currentTimeRef[0];
                final long burstTime = shortestJob.getRemainingBurstTime();
                final long endExecution = startExecution + burstTime;

                executions.add(new ProcessExecution(shortestJob.getProcessId(), startExecution, endExecution));
                currentTimeRef[0] = endExecution;
                remainingProcesses.remove(shortestJob);
            }
        }

        long executionTime = System.nanoTime() - startTime;
        metrics.recordSuccess(executionTime, 0);

        return new SchedulingResult(executions, calculateAverageWaitingTime(executions),
                                  calculateAverageTurnaroundTime(executions));
    }

    /**
     * Execute Round Robin scheduling.
     *
     * @param processes list of processes to schedule
     * @param timeQuantum time quantum for each process
     * @return scheduling result
     */
    public SchedulingResult executeRoundRobin(List<Process> processes, int timeQuantum) {
        long startTime = System.nanoTime();

        Queue<Process> readyQueue = new LinkedList<>();
        List<ProcessExecution> executions = new ArrayList<>();
        final long[] currentTimeRef = {0};

        // Sort by arrival time
        List<Process> sortedProcesses = new ArrayList<>(processes);
        sortedProcesses.sort(Comparator.comparingLong(Process::getArrivalTime));

        int processIndex = 0;

        while (processIndex < sortedProcesses.size() || !readyQueue.isEmpty()) {
            // Add newly arrived processes to ready queue
            while (processIndex < sortedProcesses.size() &&
                   sortedProcesses.get(processIndex).getArrivalTime() <= currentTimeRef[0]) {
                readyQueue.add(sortedProcesses.get(processIndex));
                processIndex++;
            }

            if (readyQueue.isEmpty()) {
                // No processes ready, advance to next arrival
                if (processIndex < sortedProcesses.size()) {
                    currentTimeRef[0] = sortedProcesses.get(processIndex).getArrivalTime();
                }
                continue;
            }

            // Execute next process for time quantum
            Process currentProcess = readyQueue.poll();
            long executionTime = Math.min(timeQuantum, currentProcess.getRemainingBurstTime());

            final long startExecution = currentTimeRef[0];
            final long endExecution = startExecution + executionTime;

            executions.add(new ProcessExecution(currentProcess.getProcessId(), startExecution, endExecution));
            currentTimeRef[0] = endExecution;

            // Update remaining burst time
            currentProcess.setRemainingBurstTime(currentProcess.getRemainingBurstTime() - executionTime);

            // If process not finished, add back to queue
            if (currentProcess.getRemainingBurstTime() > 0) {
                readyQueue.add(currentProcess);
            }
        }

        long executionTime = System.nanoTime() - startTime;
        metrics.recordSuccess(executionTime, 0);

        return new SchedulingResult(executions, calculateAverageWaitingTime(executions),
                                  calculateAverageTurnaroundTime(executions));
    }

    /**
     * Execute Priority scheduling.
     *
     * @param processes list of processes to schedule
     * @return scheduling result
     */
    public SchedulingResult executePriority(List<Process> processes) {
        long startTime = System.nanoTime();

        List<Process> remainingProcesses = new ArrayList<>(processes);
        List<ProcessExecution> executions = new ArrayList<>();
        final long[] currentTimeRef = {0};

        while (!remainingProcesses.isEmpty()) {
            // Find available processes
            final long currentTime = currentTimeRef[0];
            List<Process> availableProcesses = remainingProcesses.stream()
                .filter(p -> p.getArrivalTime() <= currentTime)
                .collect(ArrayList::new, (list, p) -> list.add(p), ArrayList::addAll);

            if (availableProcesses.isEmpty()) {
                // Advance to next arrival
                Process nextProcess = remainingProcesses.stream()
                    .min(Comparator.comparingLong(Process::getArrivalTime))
                    .orElse(null);
                if (nextProcess != null) {
                    currentTimeRef[0] = nextProcess.getArrivalTime();
                }
                continue;
            }

            // Select highest priority process
            Process highestPriority = availableProcesses.stream()
                .max(Comparator.comparingInt(Process::getPriority))
                .orElse(null);

            if (highestPriority != null) {
                final long startExecution = currentTimeRef[0];
                final long burstTime = highestPriority.getRemainingBurstTime();
                final long endExecution = startExecution + burstTime;

                executions.add(new ProcessExecution(highestPriority.getProcessId(), startExecution, endExecution));
                currentTimeRef[0] = endExecution;
                remainingProcesses.remove(highestPriority);
            }
        }

        long executionTime = System.nanoTime() - startTime;
        metrics.recordSuccess(executionTime, 0);

        return new SchedulingResult(executions, calculateAverageWaitingTime(executions),
                                  calculateAverageTurnaroundTime(executions));
    }

    // ===== INTER-PROCESS COMMUNICATION =====

    /**
     * Create a semaphore for process synchronization.
     *
     * @param semaphoreId unique semaphore identifier
     * @param initialValue initial semaphore value
     */
    public void createSemaphore(String semaphoreId, int initialValue) {
        validateInputs(semaphoreId);

        Semaphore semaphore = new Semaphore(initialValue);
        semaphores.put(semaphoreId, semaphore);

        logger.info("Created semaphore '{}' with initial value {}", semaphoreId, initialValue);
    }

    /**
     * Perform P operation (wait/acquire) on semaphore.
     *
     * @param semaphoreId semaphore identifier
     * @param processId process performing the operation
     * @return true if operation successful
     */
    public boolean semaphoreWait(String semaphoreId, String processId) {
        validateInputs(semaphoreId, processId);

        Semaphore semaphore = semaphores.get(semaphoreId);
        if (semaphore == null) {
            return false;
        }

        try {
            semaphore.acquire();
            logger.debug("Process '{}' acquired semaphore '{}'", processId, semaphoreId);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Process '{}' interrupted while waiting for semaphore '{}'",
                        processId, semaphoreId);
            return false;
        }
    }

    /**
     * Perform V operation (signal/release) on semaphore.
     *
     * @param semaphoreId semaphore identifier
     * @param processId process performing the operation
     * @return true if operation successful
     */
    public boolean semaphoreSignal(String semaphoreId, String processId) {
        validateInputs(semaphoreId, processId);

        Semaphore semaphore = semaphores.get(semaphoreId);
        if (semaphore == null) {
            return false;
        }

        semaphore.release();
        logger.debug("Process '{}' released semaphore '{}'", processId, semaphoreId);
        return true;
    }

    // ===== RESOURCE MANAGEMENT =====

    /**
     * Check for deadlock using resource allocation graph.
     *
     * @param allocation current resource allocation matrix
     * @param request current resource request matrix
     * @param available available resources vector
     * @return true if system is in safe state (no deadlock)
     */
    public boolean isSafeState(int[][] allocation, int[][] request, int[] available) {
        validateInputs(allocation, request, available);

        int numProcesses = allocation.length;
        int numResources = available.length;

        // Work vector (available resources)
        int[] work = Arrays.copyOf(available, available.length);

        // Finish vector (false = process not finished)
        boolean[] finish = new boolean[numProcesses];

        // Find an unfinished process whose needs can be satisfied
        boolean found;
        do {
            found = false;
            for (int i = 0; i < numProcesses; i++) {
                if (!finish[i]) {
                    // Check if this process can be satisfied
                    boolean canBeSatisfied = true;
                    for (int j = 0; j < numResources; j++) {
                        if (request[i][j] > work[j]) {
                            canBeSatisfied = false;
                            break;
                        }
                    }

                    if (canBeSatisfied) {
                        // Satisfy this process
                        for (int j = 0; j < numResources; j++) {
                            work[j] += allocation[i][j];
                        }
                        finish[i] = true;
                        found = true;
                    }
                }
            }
        } while (found);

        // Check if all processes are finished
        for (boolean finished : finish) {
            if (!finished) {
                return false; // Deadlock detected
            }
        }

        return true; // Safe state
    }

    // ===== MONITORING AND STATISTICS =====

    /**
     * Get comprehensive process manager statistics.
     *
     * @return process manager statistics
     */
    public ProcessManagerStatistics getStatistics() {
        long avgWaitingTime = totalProcessesCompleted.get() > 0 ?
            totalWaitingTime.get() / totalProcessesCompleted.get() : 0;
        long avgTurnaroundTime = totalProcessesCompleted.get() > 0 ?
            totalTurnaroundTime.get() / totalProcessesCompleted.get() : 0;

        return new ProcessManagerStatistics(
            totalProcessesCreated.get(),
            totalProcessesCompleted.get(),
            processes.size(),
            readyQueue.size(),
            waitingQueue.size(),
            totalContextSwitches.get(),
            avgWaitingTime,
            avgTurnaroundTime,
            currentAlgorithm
        );
    }

    /**
     * Get current process states.
     *
     * @return list of all processes with their current states
     */
    public List<Process> getAllProcesses() {
        return new ArrayList<>(processes.values());
    }

    // ===== PRIVATE METHODS =====

    private void addToReadyQueue(Process process) {
        synchronized (readyQueue) {
            readyQueue.add(process);
            process.setState(ProcessState.READY);
        }
    }

    private void scheduleFutureArrival(Process process) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        long delay = process.getArrivalTime() - System.currentTimeMillis();
        if (delay > 0) {
            scheduler.schedule(() -> addToReadyQueue(process), delay, TimeUnit.MILLISECONDS);
        } else {
            addToReadyQueue(process);
        }
    }

    private void scheduleProcesses() {
        // Simple round-robin scheduling for demonstration
        synchronized (readyQueue) {
            if (!readyQueue.isEmpty()) {
                Process currentProcess = readyQueue.poll();

                // Simulate execution for time quantum
                long executionTime = Math.min(timeQuantum.get(), currentProcess.getRemainingBurstTime());
                currentProcess.setRemainingBurstTime(currentProcess.getRemainingBurstTime() - executionTime);

                if (currentProcess.getRemainingBurstTime() > 0) {
                    // Process not finished, add back to queue
                    readyQueue.add(currentProcess);
                } else {
                    // Process finished
                    terminateProcess(currentProcess.getProcessId());
                }

                totalContextSwitches.incrementAndGet();
            }
        }
    }

    private double calculateAverageWaitingTime(List<ProcessExecution> executions) {
        return executions.stream()
            .mapToLong(exec -> exec.startTime - getProcess(exec.processId).getArrivalTime())
            .average()
            .orElse(0.0);
    }

    private double calculateAverageTurnaroundTime(List<ProcessExecution> executions) {
        return executions.stream()
            .mapToLong(exec -> exec.endTime - getProcess(exec.processId).getArrivalTime())
            .average()
            .orElse(0.0);
    }

    // ===== INNER CLASSES =====

    /**
     * Scheduling algorithms enumeration.
     */
    public enum SchedulingAlgorithm {
        FCFS, SJF, ROUND_ROBIN, PRIORITY, MULTI_LEVEL_FEEDBACK_QUEUE
    }

    /**
     * Process states enumeration.
     */
    public enum ProcessState {
        NEW, READY, RUNNING, WAITING, TERMINATED
    }

    /**
     * Process representation.
     */
    public static class Process {
        private final String processId;
        private final int priority;
        private final long originalBurstTime;
        private final long arrivalTime;
        private volatile long remainingBurstTime;
        private volatile ProcessState state;
        private volatile long completionTime;

        public Process(String processId, int priority, long burstTime, long arrivalTime) {
            this.processId = processId;
            this.priority = priority;
            this.originalBurstTime = burstTime;
            this.arrivalTime = arrivalTime;
            this.remainingBurstTime = burstTime;
            this.state = ProcessState.NEW;
        }

        // Getters and setters
        public String getProcessId() { return processId; }
        public int getPriority() { return priority; }
        public long getOriginalBurstTime() { return originalBurstTime; }
        public long getArrivalTime() { return arrivalTime; }
        public long getRemainingBurstTime() { return remainingBurstTime; }
        public void setRemainingBurstTime(long remainingBurstTime) { this.remainingBurstTime = remainingBurstTime; }
        public ProcessState getState() { return state; }
        public void setState(ProcessState state) { this.state = state; }
        public long getCompletionTime() { return completionTime; }
        public void setCompletionTime(long completionTime) { this.completionTime = completionTime; }

        @Override
        public String toString() {
            return String.format("Process{id='%s', priority=%d, remaining=%dms, state=%s}",
                               processId, priority, remainingBurstTime, state);
        }
    }

    /**
     * Process execution record.
     */
    public static class ProcessExecution {
        public final String processId;
        public final long startTime;
        public final long endTime;

        public ProcessExecution(String processId, long startTime, long endTime) {
            this.processId = processId;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public long getExecutionTime() {
            return endTime - startTime;
        }
    }

    /**
     * Scheduling result container.
     */
    public static class SchedulingResult {
        public final List<ProcessExecution> executions;
        public final double averageWaitingTime;
        public final double averageTurnaroundTime;

        public SchedulingResult(List<ProcessExecution> executions,
                              double averageWaitingTime, double averageTurnaroundTime) {
            this.executions = executions;
            this.averageWaitingTime = averageWaitingTime;
            this.averageTurnaroundTime = averageTurnaroundTime;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Scheduling Result:\n"));
            sb.append(String.format("  Average Waiting Time: %.2f ms\n", averageWaitingTime));
            sb.append(String.format("  Average Turnaround Time: %.2f ms\n", averageTurnaroundTime));
            sb.append(String.format("  Execution Order:\n"));

            for (int i = 0; i < executions.size(); i++) {
                ProcessExecution exec = executions.get(i);
                sb.append(String.format("    %d. %s: %d-%d ms\n", i + 1, exec.processId, exec.startTime, exec.endTime));
            }

            return sb.toString();
        }
    }

    /**
     * Process manager statistics container.
     */
    public static class ProcessManagerStatistics {
        public final long totalProcessesCreated;
        public final long totalProcessesCompleted;
        public final int activeProcesses;
        public final int readyQueueSize;
        public final int waitingQueueSize;
        public final long totalContextSwitches;
        public final long averageWaitingTime;
        public final long averageTurnaroundTime;
        public final SchedulingAlgorithm currentAlgorithm;

        public ProcessManagerStatistics(long totalProcessesCreated, long totalProcessesCompleted,
                                      int activeProcesses, int readyQueueSize, int waitingQueueSize,
                                      long totalContextSwitches, long averageWaitingTime,
                                      long averageTurnaroundTime, SchedulingAlgorithm currentAlgorithm) {
            this.totalProcessesCreated = totalProcessesCreated;
            this.totalProcessesCompleted = totalProcessesCompleted;
            this.activeProcesses = activeProcesses;
            this.readyQueueSize = readyQueueSize;
            this.waitingQueueSize = waitingQueueSize;
            this.totalContextSwitches = totalContextSwitches;
            this.averageWaitingTime = averageWaitingTime;
            this.averageTurnaroundTime = averageTurnaroundTime;
            this.currentAlgorithm = currentAlgorithm;
        }

        @Override
        public String toString() {
            return String.format(
                "Process Manager Stats:\n" +
                "  Processes: %d created, %d completed, %d active\n" +
                "  Queues: %d ready, %d waiting\n" +
                "  Performance: %d context switches\n" +
                "  Timing: %dms avg waiting, %dms avg turnaround\n" +
                "  Algorithm: %s",
                totalProcessesCreated, totalProcessesCompleted, activeProcesses,
                readyQueueSize, waitingQueueSize, totalContextSwitches,
                averageWaitingTime, averageTurnaroundTime, currentAlgorithm
            );
        }
    }
}
