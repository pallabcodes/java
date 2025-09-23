package com.example.account;

import com.example.sharding.RoutingDataSource;
import com.example.sharding.ShardSelector;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AccountRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ShardSelector shardSelector;

    public AccountRepository(JdbcTemplate jdbcTemplate, ShardSelector shardSelector) {
        this.jdbcTemplate = jdbcTemplate;
        this.shardSelector = shardSelector;
    }

    public Account findById(String id) {
        String shardId = shardSelector.selectShardId(id);
        try {
            RoutingDataSource.setCurrentShardId(shardId);
            return jdbcTemplate.queryForObject(
                    "select id, name, balance from account where id = ?",
                    new BeanPropertyRowMapper<>(Account.class),
                    id
            );
        } finally {
            RoutingDataSource.clear();
        }
    }
}


