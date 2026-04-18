package com.backend.designpatterns.structural.proxy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Step 4: CACHING PROXY
 */
public class Step04_CachedUserRepositoryProxy implements Step02_UserRepository {

    private final Step02_UserRepository realRepo;
    private final Map<String, Step01_User> cache = new ConcurrentHashMap<>();

    public Step04_CachedUserRepositoryProxy(Step02_UserRepository repo) {
        this.realRepo = repo;
    }

    @Override
    public Step01_User findById(String id) {
        if (cache.containsKey(id)) {
            System.out.println("CACHE HIT: " + id);
            return cache.get(id);
        }

        System.out.println("CACHE MISS: " + id);
        Step01_User user = realRepo.findById(id);
        cache.put(id, user);
        return user;
    }
}
