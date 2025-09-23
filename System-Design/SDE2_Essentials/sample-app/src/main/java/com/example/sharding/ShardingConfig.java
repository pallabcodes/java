package com.example.sharding;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class ShardingConfig {

    @Bean
    public Map<String, DataSource> shardMap() {
        Map<String, DataSource> map = new HashMap<>();
        map.put("shardA", h2("jdbc:h2:mem:shardA;DB_CLOSE_DELAY=-1"));
        map.put("shardB", h2("jdbc:h2:mem:shardB;DB_CLOSE_DELAY=-1"));
        return map;
    }

    @Bean
    public ShardSelector shardSelector(Map<String, DataSource> shardMap) {
        return new ConsistentHashSelector(shardMap, 64);
    }

    @Bean
    public DataSource dataSource(Map<String, DataSource> shardMap) {
        RoutingDataSource routing = new RoutingDataSource();
        routing.setTargetDataSources(new HashMap<>(shardMap));
        routing.setDefaultTargetDataSource(shardMap.get("shardA"));
        routing.afterPropertiesSet();
        return routing;
    }

    private DataSource h2(String url) {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl(url);
        ds.setUsername("sa");
        ds.setPassword("");
        return ds;
    }
}


