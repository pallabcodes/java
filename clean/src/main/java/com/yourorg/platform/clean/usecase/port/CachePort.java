package com.yourorg.platform.clean.usecase.port;

import java.util.Optional;

public interface CachePort {
    void put(String key, String value);
    Optional<String> get(String key);
}
