package com.netflix.productivity.modules.issues.application;

import com.netflix.productivity.modules.issues.api.IssueController.CreateIssueCommand;
import com.netflix.productivity.modules.issues.domain.Issue;
import com.netflix.productivity.modules.issues.domain.IssueRepository;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.List;

@Service
public class IssueServiceImpl implements IssueService {
    private final IssueRepository repository;
    private final Counter createdCounter;
    private final Counter listedCounter;

    public IssueServiceImpl(IssueRepository repository, MeterRegistry meterRegistry) {
        this.repository = repository;
        this.createdCounter = meterRegistry.counter("issues.created.count");
        this.listedCounter = meterRegistry.counter("issues.list.count");
    }

    @Override
    public Issue create(CreateIssueCommand command) {
        Issue issue = Issue.create(command.projectId(), command.title(), command.description());
        var saved = repository.save(issue);
        createdCounter.increment();
        return saved;
    }

    @Override
    public List<Issue> listPaged(int page, int size) {
        if (page < 0) page = 0;
        if (size < 1) size = 1;
        if (size > 100) size = 100;
        var result = repository.findPage(page, size);
        listedCounter.increment();
        return result;
    }
}


