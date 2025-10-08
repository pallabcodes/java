package com.netflix.productivity.modules.issues.application;

import com.netflix.productivity.modules.issues.api.IssueController.CreateIssueCommand;
import com.netflix.productivity.modules.issues.domain.Issue;
import com.netflix.productivity.modules.issues.domain.IssueRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IssueServiceImpl implements IssueService {
    private final IssueRepository repository;

    public IssueServiceImpl(IssueRepository repository) {
        this.repository = repository;
    }

    @Override
    public Issue create(CreateIssueCommand command) {
        Issue issue = Issue.create(command.projectId(), command.title(), command.description());
        return repository.save(issue);
    }

    @Override
    public List<Issue> listPaged(int page, int size) {
        if (page < 0) page = 0;
        if (size < 1) size = 1;
        if (size > 100) size = 100;
        return repository.findPage(page, size);
    }
}


