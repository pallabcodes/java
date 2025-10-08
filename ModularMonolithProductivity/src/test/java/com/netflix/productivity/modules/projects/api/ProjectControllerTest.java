package com.netflix.productivity.modules.projects.api;

import com.netflix.productivity.modules.projects.application.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProjectController.class)
class ProjectControllerTest {
    @Autowired MockMvc mvc;
    @MockBean ProjectService projectService;

    @Test
    void createProject() throws Exception {
        when(projectService.create(new ProjectController.CreateProjectCommand("PRJ","Name",null)))
            .thenReturn(com.netflix.productivity.modules.projects.domain.Project.rehydrate("1","PRJ","Name",null));
        mvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON)
                .content("{\"key\":\"PRJ\",\"name\":\"Name\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void listProjectsWithCaps() throws Exception {
        mvc.perform(get("/api/projects?page=0&size=1000"))
            .andExpect(status().isOk());
    }
}


