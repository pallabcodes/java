package com.backend.designpatterns.structural.proxy;

/**
 * Step 2: SUBJECT INTERFACE (User)
 */
public interface Step02_UserRepository {
    Step01_User findById(String id);
}
