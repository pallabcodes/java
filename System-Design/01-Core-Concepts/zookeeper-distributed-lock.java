package com.netflix.systemdesign.locking;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * ZookeeperDistributedLock using Curator's InterProcessMutex.
 */
public class ZookeeperDistributedLock implements AutoCloseable {
    private final InterProcessMutex mutex;
    private boolean acquired;

    public ZookeeperDistributedLock(CuratorFramework client, String path) {
        this.mutex = new InterProcessMutex(client, path);
    }

    public boolean tryLock(Duration timeout) {
        try {
            this.acquired = mutex.acquire(timeout.toMillis(), TimeUnit.MILLISECONDS);
            return acquired;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean acquired() { return acquired; }

    @Override
    public void close() {
        if (!acquired) return;
        try { mutex.release(); } catch (Exception ignored) {}
    }
}


