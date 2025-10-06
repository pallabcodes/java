package com.netflix.reporting.client;

import com.netflix.reporting.dto.CoreIssueData;
import com.netflix.reporting.dto.CoreProjectData;
import com.netflix.reporting.dto.CoreUserData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.OffsetDateTime;
import java.util.List;

@FeignClient(name = "core-service", url = "${core.service.url:http://localhost:8080}")
public interface CoreServiceClient {
    
    @GetMapping("/api/internal/issues")
    List<CoreIssueData> getIssues(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam("projectId") String projectId,
            @RequestParam("fromDate") OffsetDateTime fromDate,
            @RequestParam("toDate") OffsetDateTime toDate
    );
    
    @GetMapping("/api/internal/projects")
    List<CoreProjectData> getProjects(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam("projectId") String projectId
    );
    
    @GetMapping("/api/internal/users")
    List<CoreUserData> getUsers(
            @RequestHeader("X-Tenant-ID") String tenantId
    );
}
