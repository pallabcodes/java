package com.netflix.productivity.importexport.controller;

import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ResponseMapper;
import com.netflix.productivity.importexport.service.ImportExportService;
import com.netflix.productivity.security.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/import-export")
@Tag(name = "Import/Export", description = "Data import and export endpoints")
public class ImportExportController {
    private final ImportExportService importExportService;
    private final ResponseMapper responses;
    
    @PostMapping("/issues/import")
    @RequirePermission("TENANT_ADMIN")
    @Operation(summary = "Import issues from NDJSON file", 
               description = "Import issues from a newline-delimited JSON file")
    public ResponseEntity<ApiResponse<ImportExportService.ImportResult>> importIssues(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Parameter(description = "NDJSON file containing issues")
            @RequestParam("file") MultipartFile file) {
        
        if (file.isEmpty()) {
            return responses.badRequest("File is empty");
        }
        
        if (!file.getOriginalFilename().toLowerCase().endsWith(".ndjson") && 
            !file.getOriginalFilename().toLowerCase().endsWith(".jsonl")) {
            return responses.badRequest("File must be NDJSON format (.ndjson or .jsonl)");
        }
        
        ImportExportService.ImportResult result = importExportService.importIssues(tenantId, file);
        return responses.ok(result);
    }
    
    @PostMapping("/projects/import")
    @RequirePermission("TENANT_ADMIN")
    @Operation(summary = "Import projects from NDJSON file", 
               description = "Import projects from a newline-delimited JSON file")
    public ResponseEntity<ApiResponse<ImportExportService.ImportResult>> importProjects(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Parameter(description = "NDJSON file containing projects")
            @RequestParam("file") MultipartFile file) {
        
        if (file.isEmpty()) {
            return responses.badRequest("File is empty");
        }
        
        if (!file.getOriginalFilename().toLowerCase().endsWith(".ndjson") && 
            !file.getOriginalFilename().toLowerCase().endsWith(".jsonl")) {
            return responses.badRequest("File must be NDJSON format (.ndjson or .jsonl)");
        }
        
        ImportExportService.ImportResult result = importExportService.importProjects(tenantId, file);
        return responses.ok(result);
    }
    
    @GetMapping(value = "/issues/export", produces = "application/x-ndjson")
    @RequirePermission("TENANT_USER")
    @Operation(summary = "Export issues as NDJSON", 
               description = "Export issues as a streaming NDJSON file")
    public ResponseEntity<InputStreamResource> exportIssues(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Parameter(description = "Project ID to filter by (optional)")
            @RequestParam(required = false) String projectId) {
        
        try {
            String filename = String.format("issues_export_%s_%s.ndjson", 
                tenantId, 
                OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            
            // Convert stream to byte array for response
            StringBuilder content = new StringBuilder();
            importExportService.exportIssues(tenantId, projectId)
                .forEach(line -> content.append(line).append("\n"));
            
            ByteArrayInputStream bis = new ByteArrayInputStream(
                content.toString().getBytes(StandardCharsets.UTF_8));
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(bis));
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping(value = "/projects/export", produces = "application/x-ndjson")
    @RequirePermission("TENANT_USER")
    @Operation(summary = "Export projects as NDJSON", 
               description = "Export projects as a streaming NDJSON file")
    public ResponseEntity<InputStreamResource> exportProjects(
            @RequestHeader("X-Tenant-ID") String tenantId) {
        
        try {
            String filename = String.format("projects_export_%s_%s.ndjson", 
                tenantId, 
                OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            
            // Convert stream to byte array for response
            StringBuilder content = new StringBuilder();
            importExportService.exportProjects(tenantId)
                .forEach(line -> content.append(line).append("\n"));
            
            ByteArrayInputStream bis = new ByteArrayInputStream(
                content.toString().getBytes(StandardCharsets.UTF_8));
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(bis));
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping(value = "/issues/stream", produces = "application/x-ndjson")
    @RequirePermission("TENANT_USER")
    @Operation(summary = "Stream issues as NDJSON", 
               description = "Stream issues as NDJSON for large datasets")
    public ResponseEntity<InputStreamResource> streamIssues(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Parameter(description = "Project ID to filter by (optional)")
            @RequestParam(required = false) String projectId) {
        
        try {
            // For streaming, we need to implement a custom response body writer
            // This is a simplified version that collects all data first
            StringBuilder content = new StringBuilder();
            importExportService.exportIssues(tenantId, projectId)
                .forEach(line -> content.append(line).append("\n"));
            
            ByteArrayInputStream bis = new ByteArrayInputStream(
                content.toString().getBytes(StandardCharsets.UTF_8));
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(bis));
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
