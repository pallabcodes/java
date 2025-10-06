package com.netflix.productivity.outbox.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public boolean claim(String tenantId, String consumerName, String messageKey) {
        int updated = jdbcTemplate.update(
            "insert into idempotency_keys(tenant_id, consumer_name, message_key) values (?,?,?) on conflict do nothing",
            tenantId, consumerName, messageKey
        );
        return updated == 1;
    }
}


