package com.netflix.productivity.modules.projects.application;

import com.netflix.productivity.modules.projects.api.ProjectController.CreateProjectCommand;
import com.netflix.productivity.modules.projects.domain.Project;
import com.netflix.productivity.modules.projects.domain.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository repository;

    public ProjectServiceImpl(ProjectRepository repository) {
        this.repository = repository;
    }

    @Override
    public Project create(CreateProjectCommand command) {
        Project project = Project.create(command.key(), command.name(), command.description());
        return repository.save(project);
    }

    @Override
    public List<Project> list() {
        return repository.findAll();
    }

    @Override
    public List<Project> listPaged(int page, int size) {
        if (page < 0) page = 0;
        if (size < 1) size = 1;
        if (size > 100) size = 100;
        return repository.findPage(page, size);
    }
}


