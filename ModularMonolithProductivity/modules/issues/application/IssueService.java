package com.netflix.productivity.modules.issues.application;

import com.netflix.productivity.modules.issues.api.IssueController.CreateIssueCommand;
import com.netflix.productivity.modules.issues.domain.Issue;

import java.util.List;

public interface IssueService {
    Issue create(CreateIssueCommand command);
    List<Issue> listPaged(int page, int size);
}


