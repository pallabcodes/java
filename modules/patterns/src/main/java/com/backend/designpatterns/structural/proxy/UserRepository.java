package com.backend.designpatterns.structural.proxy;

// Role: Subject Interface
public interface UserRepository {
    User findById(String id);
}
