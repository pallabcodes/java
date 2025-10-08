package com.netflix.productivity.modules.projects.infrastructure;

import com.netflix.productivity.modules.projects.domain.Project;
import com.netflix.productivity.modules.projects.domain.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(ProjectRepositoryAdapter.class)
class ProjectRepositoryAdapterTest {
    @Autowired ProjectJpaRepository jpa;
    @Autowired ProjectRepository repository;

    @Test
    void saveAndFindPage() {
        var p = Project.create("PRJ","Name",null);
        var saved = repository.save(p);
        assertThat(saved.getId()).isNotBlank();
        var page = repository.findPage(0, 10);
        assertThat(page).isNotEmpty();
    }
}


