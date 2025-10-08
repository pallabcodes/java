package com.netflix.productivity.modules.projects.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.netflix.productivity.modules.projects.application.ProjectService;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@Validated
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody CreateProjectRequest request) {
        var project = projectService.create(new CreateProjectCommand(request.key(), request.name(), request.description()));
        return ResponseEntity.ok(ProjectResponse.from(project));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        int capped = Math.min(Math.max(size, 1), 100);
        return ResponseEntity.ok(projectService.listPaged(page, capped).stream().map(ProjectResponse::from).toList());
    }

    public record CreateProjectRequest(@NotBlank @Size(max = 16) String key,
                                       @NotBlank @Size(max = 128) String name,
                                       @Size(max = 1024) String description) {}

    public record ProjectResponse(String id, String key, String name, String description) {
        static ProjectResponse from(com.netflix.productivity.modules.projects.domain.Project project) {
            return new ProjectResponse(project.getId(), project.getKey(), project.getName(), project.getDescription());
        }
    }

    public record CreateProjectCommand(String key, String name, String description) {}
}


