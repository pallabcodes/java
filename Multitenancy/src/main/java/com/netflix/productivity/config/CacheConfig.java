package com.netflix.productivity.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
public class CacheConfig {

    @Value("${cache.issue.ttlSeconds:60}")
    private long issueTtlSeconds;

    @Value("${cache.issue.maxSize:10000}")
    private long issueMaxSize;

    @Value("${cache.project.ttlSeconds:120}")
    private long projectTtlSeconds;

    @Value("${cache.project.maxSize:2000}")
    private long projectMaxSize;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        Caffeine<Object, Object> issueSpec = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(issueTtlSeconds))
                .maximumSize(issueMaxSize);
        Caffeine<Object, Object> projectSpec = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(projectTtlSeconds))
                .maximumSize(projectMaxSize);

        // Define caches explicitly to allow different specs per cache
        List<CaffeineCache> caches = List.of(
                new CaffeineCache("ISSUE_LIST", issueSpec.build()),
                new CaffeineCache("ISSUE_LIST_BY_PROJECT", issueSpec.build()),
                new CaffeineCache("ISSUE_BY_KEY", issueSpec.build()),
                new CaffeineCache("PROJECT_LIST", projectSpec.build()),
                new CaffeineCache("PROJECT_BY_KEY", projectSpec.build())
        );

        manager.setCaches(caches);
        return manager;
    }
}
package com.netflix.productivity.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public Caffeine<Object, Object> caffeineSpec() {
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(10_000);
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(caffeine);
        manager.setAllowNullValues(false);
        return manager;
    }
}


