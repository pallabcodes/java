package com.netflix.productivity.modules.projects.application;

import com.netflix.productivity.modules.projects.api.ProjectController.CreateProjectCommand;
import com.netflix.productivity.modules.projects.domain.Project;

import java.util.List;

public interface ProjectService {
    Project create(CreateProjectCommand command);
    List<Project> list();
    List<Project> listPaged(int page, int size);
}


