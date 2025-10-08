package com.netflix.productivity.modules.issues.api;

import com.netflix.productivity.modules.issues.application.IssueService;
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

@WebMvcTest(controllers = IssueController.class)
class IssueControllerTest {
    @Autowired MockMvc mvc;
    @MockBean IssueService issueService;

    @Test
    void createIssue() throws Exception {
        when(issueService.create(new IssueController.CreateIssueCommand("1","title",null)))
            .thenReturn(com.netflix.productivity.modules.issues.domain.Issue.rehydrate("1","1","title",null,"OPEN"));
        mvc.perform(post("/api/issues").contentType(MediaType.APPLICATION_JSON)
                .content("{\"projectId\":\"1\",\"title\":\"title\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void listIssuesWithCaps() throws Exception {
        mvc.perform(get("/api/issues?page=0&size=1000"))
            .andExpect(status().isOk());
    }
}


