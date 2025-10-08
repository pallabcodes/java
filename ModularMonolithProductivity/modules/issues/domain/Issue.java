package com.netflix.productivity.modules.issues.domain;

public class Issue {
    private final String id;
    private final String projectId;
    private final String title;
    private final String description;
    private final String status;

    private Issue(String id, String projectId, String title, String description, String status) {
        this.id = id;
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public static Issue create(String projectId, String title, String description) {
        return new Issue(java.util.UUID.randomUUID().toString(), projectId, title, description, "OPEN");
    }

    public static Issue rehydrate(String id, String projectId, String title, String description, String status) {
        return new Issue(id, projectId, title, description, status);
    }

    public String getId() { return id; }
    public String getProjectId() { return projectId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
}


