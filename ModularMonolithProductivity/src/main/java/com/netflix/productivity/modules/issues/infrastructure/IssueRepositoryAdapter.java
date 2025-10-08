package com.netflix.productivity.modules.issues.infrastructure;

import com.netflix.productivity.modules.issues.domain.Issue;
import com.netflix.productivity.modules.issues.domain.IssueRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IssueRepositoryAdapter implements IssueRepository {
    private final IssueJpaRepository jpa;

    public IssueRepositoryAdapter(IssueJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Issue save(Issue issue) {
        IssueEntity e = toEntity(issue);
        IssueEntity saved = jpa.save(e);
        return toDomain(saved);
    }

    @Override
    public List<Issue> findPage(int page, int size) {
        return jpa.findAll(PageRequest.of(page, size)).stream().map(this::toDomain).toList();
    }

    private IssueEntity toEntity(Issue i) {
        IssueEntity e = new IssueEntity();
        e.setId(i.getId());
        e.setProjectId(i.getProjectId());
        e.setTitle(i.getTitle());
        e.setDescription(i.getDescription());
        e.setStatus(i.getStatus());
        return e;
    }

    private Issue toDomain(IssueEntity e) {
        return Issue.rehydrate(e.getId(), e.getProjectId(), e.getTitle(), e.getDescription(), e.getStatus());
    }
}


