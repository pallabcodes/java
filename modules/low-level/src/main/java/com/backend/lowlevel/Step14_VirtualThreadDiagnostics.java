package com.backend.lowlevel;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Step 14: Virtual Thread Diagnostics (Pinning & Unmounting)
 * 
 * L7 Mastery:
 * 1. Mounting/Unmounting: Virtual threads should "unmount" from carrier threads during I/O.
 * 2. Pinning: 'synchronized' blocks prevent unmounting, hogging the OS carrier thread.
 * 3. Observability: Using JFR or system properties to detect pinning in production.
 */
public class Step14_VirtualThreadDiagnostics {

    private static final ReentrantLock lock = new ReentrantLock();
    private static final Object syncObject = new Object();

    public static void runWithLock() {
        lock.lock();
        try {
            // L7 Note: VirtualThread will unmount here, freeing the carrier thread for others.
            System.out.println(Thread.currentThread() + " - Sleeping with ReentrantLock (Expected: Unmount)");
            Thread.sleep(Duration.ofMillis(100));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    public static void runWithSynchronized() {
        synchronized (syncObject) {
            // L7 Note: VirtualThread is PINNED to the carrier thread here.
            System.out.println(Thread.currentThread() + " - Sleeping with synchronized (Expected: PINNING)");
            try {
                Thread.sleep(Duration.ofMillis(100));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Step 14: Virtual Thread Diagnostics (Pinning vs Unmounting) ===");
        System.out.println("L7 Setup: Run with '-Djdk.tracePinnedThreads=full' to see the exact stack trace when pinning occurs.\n");

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // Task 1: ReentrantLock (The Scalable Way)
            executor.submit(Step14_VirtualThreadDiagnostics::runWithLock);
            
            // Task 2: synchronized (The Legacy Way - Potential Bottleneck)
            executor.submit(Step14_VirtualThreadDiagnostics::runWithSynchronized);
        }

        System.out.println("\nL5 Insight: 'synchronized' still works but kills the scaling benefit of Virtual Threads.");
        System.out.println("L7 Depth: In high-throughput systems, pinning can lead to starvation of the Carrier Thread Pool (ForkJoinPool).");
    }
}
