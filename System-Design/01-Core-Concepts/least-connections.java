package com.netflix.systemdesign.loadbalancing;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LeastConnections load balancer.
 * Picks the backend with the fewest active connections.
 */
public class LeastConnections {
    public static final class Backend {
        public final String id;
        public Backend(String id) { this.id = id; }
        @Override public String toString() { return id; }
    }

    private volatile List<Backend> backends;
    private final ConcurrentMap<String, AtomicInteger> active = new ConcurrentHashMap<>();

    public LeastConnections(List<Backend> backends) {
        configure(backends);
    }

    public synchronized void configure(List<Backend> newBackends) {
        Objects.requireNonNull(newBackends);
        this.backends = List.copyOf(newBackends);
        for (Backend b : backends) active.putIfAbsent(b.id, new AtomicInteger());
        // remove old
        active.keySet().retainAll(backends.stream().map(b -> b.id).toList());
    }

    public Backend pick() {
        Backend candidate = null;
        int best = Integer.MAX_VALUE;
        for (Backend b : backends) {
            int a = active.get(b.id).get();
            if (a < best) { best = a; candidate = b; }
        }
        return candidate;
    }

    public AutoCloseable acquire(Backend b) {
        active.get(b.id).incrementAndGet();
        return () -> active.get(b.id).decrementAndGet();
    }
}


