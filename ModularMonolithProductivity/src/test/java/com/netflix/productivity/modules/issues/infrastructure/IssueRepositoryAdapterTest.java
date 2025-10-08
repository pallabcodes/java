package com.netflix.productivity.modules.issues.infrastructure;

import com.netflix.productivity.modules.issues.domain.Issue;
import com.netflix.productivity.modules.issues.domain.IssueRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(IssueRepositoryAdapter.class)
class IssueRepositoryAdapterTest {
    @Autowired IssueJpaRepository jpa;
    @Autowired IssueRepository repository;

    @Test
    void saveAndFindPage() {
        var i = Issue.create("1","title",null);
        var saved = repository.save(i);
        assertThat(saved.getId()).isNotBlank();
        var page = repository.findPage(0, 10);
        assertThat(page).isNotEmpty();
    }
}


