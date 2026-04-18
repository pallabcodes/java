package com.backend.designpatterns.realworld.legacy_wrapper;

import java.util.HashMap;
import java.util.Map;

// Proxy (Caching)
public class CachingProxy implements ModernService {
    private final ModernService realService;
    private final Map<String, String> cache = new HashMap<>();

    public CachingProxy(ModernService realService) {
        this.realService = realService;
    }

    @Override
    public String getData(String id) {
        if (cache.containsKey(id)) {
            System.out.println("Proxy: Cache hit for " + id);
            return cache.get(id);
        }
        System.out.println("Proxy: Cache miss for " + id);
        String data = realService.getData(id);
        cache.put(id, data);
        return data;
    }
}
