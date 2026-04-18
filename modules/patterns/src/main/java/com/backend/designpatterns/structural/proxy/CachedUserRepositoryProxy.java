package com.backend.designpatterns.structural.proxy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Role: Proxy
public class CachedUserRepositoryProxy implements UserRepository {

    private final UserRepository realRepo;
    private final Map<String, User> cache = new ConcurrentHashMap<>();

    public CachedUserRepositoryProxy(UserRepository repo) {
        this.realRepo = repo;
    }

    @Override
    public User findById(String id) {
        if (cache.containsKey(id)) {
            System.out.println("CACHE HIT: " + id);
            return cache.get(id);
        }

        System.out.println("CACHE MISS: " + id);
        User user = realRepo.findById(id);
        cache.put(id, user);
        return user;
    }
}
