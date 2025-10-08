package com.netflix.productivity.modules.projects.domain;

public class Project {
    private final String id;
    private final String key;
    private final String name;
    private final String description;

    private Project(String id, String key, String name, String description) {
        this.id = id;
        this.key = key;
        this.name = name;
        this.description = description;
    }

    public static Project create(String key, String name, String description) {
        return new Project(java.util.UUID.randomUUID().toString(), key, name, description);
    }

    public static Project rehydrate(String id, String key, String name, String description) {
        return new Project(id, key, name, description);
    }

    public String getId() { return id; }
    public String getKey() { return key; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}


