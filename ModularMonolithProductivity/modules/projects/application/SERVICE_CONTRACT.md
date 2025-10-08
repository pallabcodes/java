# Projects Service Contract

## Operations

- createProject(CreateProjectCommand) → Project
- updateProject(ProjectId, UpdateProjectCommand) → Project
- deleteProject(ProjectId) → void
- getProject(ProjectId) → Project
- listProjects(ProjectQuery) → Page<Project>

## Policies

- name must be unique within tenant
- key must match pattern and be unique within tenant
- page size capped at one hundred
