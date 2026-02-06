package com.backend.designpatterns.structural.proxy;

public class ProxyDemo {

    public static void main(String[] args) {
        System.out.println("--- Proxy Pattern Demo ---");

        // Use Case: Use Proxy to control access to an object, such as lazy loading, 
        // access control/security, or caching results of expensive operations.

        UserRepository repo = new CachedUserRepositoryProxy(new DatabaseUserRepository());

        // 1. First fetch (Slow)
        long start1 = System.currentTimeMillis();
        System.out.println(repo.findById("101"));
        System.out.println("Time: " + (System.currentTimeMillis() - start1) + "ms");

        // 2. Second fetch (Fast - Cache Hit)
        long start2 = System.currentTimeMillis();
        System.out.println(repo.findById("101"));
        System.out.println("Time: " + (System.currentTimeMillis() - start2) + "ms");
    }
}
