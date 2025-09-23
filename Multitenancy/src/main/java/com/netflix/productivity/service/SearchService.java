package com.netflix.productivity.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    private final JdbcTemplate jdbcTemplate;

    public SearchService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> searchIssues(String tenantId, String query, int limit) {
        int capped = Math.min(Math.max(limit, 1), 50);
        String sql = "select id, key, title, project_id, status, priority from issues " +
                "where tenant_id = ? and deleted_at is null and (" +
                "lower(title) % lower(?) or lower(description) % lower(?) or " +
                "to_tsvector('simple', coalesce(description,'')) @@ plainto_tsquery(?)) " +
                "order by (similarity(lower(title), lower(?)) * 1.5 + similarity(lower(description), lower(?))) desc nulls last " +
                "limit ?";
        return jdbcTemplate.queryForList(sql, tenantId, query, query, query, query, query, capped);
    }
}


