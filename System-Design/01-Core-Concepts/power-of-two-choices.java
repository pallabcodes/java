package com.netflix.systemdesign.loadbalancing;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Power of Two Choices load balancer.
 * Randomly samples two backends and picks the one with fewer active connections.
 */
public class PowerOfTwoChoices {
    public static final class Backend {
        public final String id;
        public Backend(String id) { this.id = id; }
        @Override public String toString() { return id; }
    }

    private final Random rnd = new Random();
    private volatile List<Backend> backends;
    private final ConcurrentMap<String, AtomicInteger> active = new ConcurrentHashMap<>();

    public PowerOfTwoChoices(List<Backend> backends) { configure(backends); }

    public synchronized void configure(List<Backend> newBackends) {
        Objects.requireNonNull(newBackends);
        this.backends = List.copyOf(newBackends);
        for (Backend b : backends) active.putIfAbsent(b.id, new AtomicInteger());
        active.keySet().retainAll(backends.stream().map(b -> b.id).toList());
    }

    public Backend pick() {
        if (backends.size() == 1) return backends.get(0);
        Backend a = backends.get(rnd.nextInt(backends.size()));
        Backend b = backends.get(rnd.nextInt(backends.size()));
        int ca = active.get(a.id).get();
        int cb = active.get(b.id).get();
        return ca <= cb ? a : b;
    }

    public AutoCloseable acquire(Backend b) {
        active.get(b.id).incrementAndGet();
        return () -> active.get(b.id).decrementAndGet();
    }
}


