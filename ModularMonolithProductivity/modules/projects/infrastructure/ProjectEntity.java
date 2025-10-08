package com.netflix.productivity.modules.projects.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "projects")
public class ProjectEntity {
    @Id
    private String id;
    @Column(nullable = false, length = 16)
    private String key;
    @Column(nullable = false, length = 128)
    private String name;
    @Column(length = 1024)
    private String description;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}


