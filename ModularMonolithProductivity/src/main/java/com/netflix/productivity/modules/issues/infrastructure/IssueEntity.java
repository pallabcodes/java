package com.netflix.productivity.modules.issues.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "issues")
public class IssueEntity {
    @Id
    private String id;
    @Column(nullable = false)
    private String projectId;
    @Column(nullable = false, length = 256)
    private String title;
    @Column(length = 2048)
    private String description;
    @Column(nullable = false, length = 32)
    private String status;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}


