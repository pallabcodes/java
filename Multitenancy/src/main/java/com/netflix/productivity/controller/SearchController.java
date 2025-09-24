package com.netflix.productivity.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ResponseMapper;
import com.netflix.productivity.service.SearchService;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;
    private final ResponseMapper responseMapper;

    public SearchController(SearchService searchService, ResponseMapper responseMapper) {
        this.searchService = searchService;
        this.responseMapper = responseMapper;
    }

    @GetMapping("/issues")
    public ResponseEntity<ApiResponse<Object>> searchIssues(@RequestHeader("X-Tenant-ID") String tenantId,
                                                            @RequestParam("q") String query,
                                                            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        final String normalizedQuery = normalizeQuery(query);
        final int effectiveLimit = normalizeLimit(limit);
        return responseMapper.ok(searchService.searchIssues(tenantId, normalizedQuery, effectiveLimit));
    }

    private String normalizeQuery(String query) {
        return query == null ? "" : query.trim();
    }

    private int normalizeLimit(int limit) {
        if (limit < 1) return 1;
        return Math.min(limit, 100);
    }
}


