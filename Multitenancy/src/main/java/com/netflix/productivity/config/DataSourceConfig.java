package com.netflix.productivity.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Value("${db.pool.maxSize:20}")
    private int maxPoolSize;

    @Value("${db.pool.minIdle:5}")
    private int minIdle;

    @Value("${db.pool.idleTimeoutMs:60000}")
    private long idleTimeoutMs;

    @Value("${db.pool.maxLifetimeMs:1800000}")
    private long maxLifetimeMs;

    @Bean
    public DataSource dataSource() {
        HikariConfig cfg = new HikariConfig();
        // Rely on Spring Boot URL, username, password properties via system properties
        cfg.setMaximumPoolSize(maxPoolSize);
        cfg.setMinimumIdle(minIdle);
        cfg.setIdleTimeout(idleTimeoutMs);
        cfg.setMaxLifetime(maxLifetimeMs);
        cfg.addDataSourceProperty("reWriteBatchedInserts", true);
        cfg.addDataSourceProperty("preparedStatementCacheQueries", 256);
        cfg.addDataSourceProperty("preparedStatementCacheSizeMiB", 4);
        cfg.addDataSourceProperty("stringtype", "unspecified");
        // Statement timeout at driver level (ms)
        cfg.addDataSourceProperty("socketTimeout", 10_000);
        cfg.addDataSourceProperty("loginTimeout", 5);
        cfg.setConnectionInitSql("set statement_timeout = 5000; set idle_in_transaction_session_timeout = 10000");
        return new HikariDataSource(cfg);
    }
}

