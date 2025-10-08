package com.netflix.productivity.modules.issues.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.netflix.productivity.modules.issues.application.IssueService;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
@Validated
public class IssueController {
    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @PostMapping
    public ResponseEntity<IssueResponse> create(@Valid @RequestBody CreateIssueRequest request) {
        var issue = issueService.create(new CreateIssueCommand(request.projectId(), request.title(), request.description()));
        return ResponseEntity.ok(IssueResponse.from(issue));
    }

    @GetMapping
    public ResponseEntity<List<IssueResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        int capped = Math.min(Math.max(size, 1), 100);
        return ResponseEntity.ok(issueService.listPaged(page, capped).stream().map(IssueResponse::from).toList());
    }

    public record CreateIssueRequest(@NotBlank String projectId,
                                     @NotBlank @Size(max = 256) String title,
                                     @Size(max = 2048) String description) {}

    public record IssueResponse(String id, String projectId, String title, String description, String status) {
        static IssueResponse from(com.netflix.productivity.modules.issues.domain.Issue issue) {
            return new IssueResponse(issue.getId(), issue.getProjectId(), issue.getTitle(), issue.getDescription(), issue.getStatus());
        }
    }

    public record CreateIssueCommand(String projectId, String title, String description) {}
}


