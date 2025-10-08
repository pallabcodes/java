package com.netflix.productivity.modules.issues.domain;

import java.util.List;

public interface IssueRepository {
    Issue save(Issue issue);
    List<Issue> findPage(int page, int size);
}


