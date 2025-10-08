package com.netflix.productivity.modules.projects.infrastructure;

import com.netflix.productivity.modules.projects.domain.Project;
import com.netflix.productivity.modules.projects.domain.ProjectRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjectRepositoryAdapter implements ProjectRepository {
    private final ProjectJpaRepository jpa;

    public ProjectRepositoryAdapter(ProjectJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Project save(Project project) {
        ProjectEntity entity = toEntity(project);
        ProjectEntity saved = jpa.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<Project> findAll() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Project> findPage(int page, int size) {
        return jpa.findAll(PageRequest.of(page, size)).stream().map(this::toDomain).toList();
    }

    private ProjectEntity toEntity(Project p) {
        ProjectEntity e = new ProjectEntity();
        e.setId(p.getId());
        e.setKey(p.getKey());
        e.setName(p.getName());
        e.setDescription(p.getDescription());
        return e;
    }

    private Project toDomain(ProjectEntity e) {
        return Project.rehydrate(e.getId(), e.getKey(), e.getName(), e.getDescription());
    }
}


