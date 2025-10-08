package com.netflix.productivity.modules.projects.domain;

import java.util.List;

public interface ProjectRepository {
    Project save(Project project);
    List<Project> findAll();
    List<Project> findPage(int page, int size);
}


