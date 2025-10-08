package com.netflix.productivity.modules.projects.application;

import com.netflix.productivity.modules.projects.api.ProjectController.CreateProjectCommand;
import com.netflix.productivity.modules.projects.domain.Project;
import com.netflix.productivity.modules.projects.domain.ProjectRepository;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository repository;
    private final Counter createdCounter;
    private final Counter listedCounter;

    public ProjectServiceImpl(ProjectRepository repository, MeterRegistry meterRegistry) {
        this.repository = repository;
        this.createdCounter = meterRegistry.counter("projects.created.count");
        this.listedCounter = meterRegistry.counter("projects.list.count");
    }

    @Override
    public Project create(CreateProjectCommand command) {
        Project project = Project.create(command.key(), command.name(), command.description());
        var saved = repository.save(project);
        createdCounter.increment();
        return saved;
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
        var result = repository.findPage(page, size);
        listedCounter.increment();
        return result;
    }
}


