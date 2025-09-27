package com.netflix.productivity.importexport.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.netflix.productivity.entity.Issue;
import com.netflix.productivity.entity.Project;
import com.netflix.productivity.repository.IssueRepository;
import com.netflix.productivity.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportExportService {
    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final ObjectMapper objectMapper;
    
    public ImportExportService(IssueRepository issueRepository, ProjectRepository projectRepository) {
        this.issueRepository = issueRepository;
        this.projectRepository = projectRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Transactional
    public ImportResult importIssues(String tenantId, MultipartFile file) {
        log.info("Starting import of issues for tenant {} from file {}", tenantId, file.getOriginalFilename());
        
        ImportResult result = new ImportResult();
        List<String> errors = new ArrayList<>();
        int processed = 0;
        int successful = 0;
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                processed++;
                try {
                    Issue issue = objectMapper.readValue(line, Issue.class);
                    issue.setTenantId(tenantId);
                    issue.setId(null); // Let JPA generate new ID
                    
                    // Validate required fields
                    if (issue.getTitle() == null || issue.getTitle().trim().isEmpty()) {
                        errors.add("Row " + processed + ": Title is required");
                        continue;
                    }
                    
                    if (issue.getProjectId() == null) {
                        errors.add("Row " + processed + ": Project ID is required");
                        continue;
                    }
                    
                    // Verify project exists
                    if (!projectRepository.existsByIdAndTenantId(issue.getProjectId(), tenantId)) {
                        errors.add("Row " + processed + ": Project " + issue.getProjectId() + " not found");
                        continue;
                    }
                    
                    issueRepository.save(issue);
                    successful++;
                    
                } catch (JsonProcessingException e) {
                    errors.add("Row " + processed + ": Invalid JSON - " + e.getMessage());
                } catch (Exception e) {
                    errors.add("Row " + processed + ": " + e.getMessage());
                }
            }
            
        } catch (IOException e) {
            log.error("Error reading import file", e);
            errors.add("Error reading file: " + e.getMessage());
        }
        
        result.setProcessed(processed);
        result.setSuccessful(successful);
        result.setErrors(errors);
        
        log.info("Import completed: {} processed, {} successful, {} errors", 
                processed, successful, errors.size());
        
        return result;
    }
    
    @Transactional
    public ImportResult importProjects(String tenantId, MultipartFile file) {
        log.info("Starting import of projects for tenant {} from file {}", tenantId, file.getOriginalFilename());
        
        ImportResult result = new ImportResult();
        List<String> errors = new ArrayList<>();
        int processed = 0;
        int successful = 0;
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                processed++;
                try {
                    Project project = objectMapper.readValue(line, Project.class);
                    project.setTenantId(tenantId);
                    project.setId(null); // Let JPA generate new ID
                    
                    // Validate required fields
                    if (project.getName() == null || project.getName().trim().isEmpty()) {
                        errors.add("Row " + processed + ": Name is required");
                        continue;
                    }
                    
                    projectRepository.save(project);
                    successful++;
                    
                } catch (JsonProcessingException e) {
                    errors.add("Row " + processed + ": Invalid JSON - " + e.getMessage());
                } catch (Exception e) {
                    errors.add("Row " + processed + ": " + e.getMessage());
                }
            }
            
        } catch (IOException e) {
            log.error("Error reading import file", e);
            errors.add("Error reading file: " + e.getMessage());
        }
        
        result.setProcessed(processed);
        result.setSuccessful(successful);
        result.setErrors(errors);
        
        log.info("Import completed: {} processed, {} successful, {} errors", 
                processed, successful, errors.size());
        
        return result;
    }
    
    public Stream<String> exportIssues(String tenantId, String projectId) {
        log.info("Starting export of issues for tenant {} and project {}", tenantId, projectId);
        
        return issueRepository.findByTenantIdAndProjectId(tenantId, projectId)
            .stream()
            .map(issue -> {
                try {
                    return objectMapper.writeValueAsString(issue);
                } catch (JsonProcessingException e) {
                    log.error("Error serializing issue {}", issue.getId(), e);
                    return "{}"; // Return empty JSON on error
                }
            });
    }
    
    public Stream<String> exportProjects(String tenantId) {
        log.info("Starting export of projects for tenant {}", tenantId);
        
        return projectRepository.findByTenantId(tenantId)
            .stream()
            .map(project -> {
                try {
                    return objectMapper.writeValueAsString(project);
                } catch (JsonProcessingException e) {
                    log.error("Error serializing project {}", project.getId(), e);
                    return "{}"; // Return empty JSON on error
                }
            });
    }
    
    public static class ImportResult {
        private int processed;
        private int successful;
        private List<String> errors = new ArrayList<>();
        
        public int getProcessed() { return processed; }
        public void setProcessed(int processed) { this.processed = processed; }
        
        public int getSuccessful() { return successful; }
        public void setSuccessful(int successful) { this.successful = successful; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        
        public int getFailed() { return processed - successful; }
        public boolean hasErrors() { return !errors.isEmpty(); }
    }
}
