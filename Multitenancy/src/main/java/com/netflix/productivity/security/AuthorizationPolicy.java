package com.netflix.productivity.security;

import com.netflix.productivity.entity.Issue;
import com.netflix.productivity.entity.Project;
import com.netflix.productivity.entity.User;
import com.netflix.productivity.repository.IssueRepository;
import com.netflix.productivity.repository.ProjectRepository;
import com.netflix.productivity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizationPolicy {
    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    
    /**
     * Check if user can access a project
     */
    public boolean canAccessProject(String tenantId, String userId, String projectId) {
        try {
            // Check if user exists and belongs to tenant
            Optional<User> userOpt = userRepository.findByIdAndTenantId(userId, tenantId);
            if (userOpt.isEmpty()) {
                log.warn("User {} not found in tenant {}", userId, tenantId);
                return false;
            }
            
            // Check if project exists and belongs to tenant
            Optional<Project> projectOpt = projectRepository.findByIdAndTenantId(projectId, tenantId);
            if (projectOpt.isEmpty()) {
                log.warn("Project {} not found in tenant {}", projectId, tenantId);
                return false;
            }
            
            // For now, all users in tenant can access all projects
            // In future, implement project-level permissions
            return true;
            
        } catch (Exception e) {
            log.error("Error checking project access for user {} to project {}", userId, projectId, e);
            return false;
        }
    }
    
    /**
     * Check if user can access an issue
     */
    public boolean canAccessIssue(String tenantId, String userId, String issueId) {
        try {
            // Check if user exists and belongs to tenant
            Optional<User> userOpt = userRepository.findByIdAndTenantId(userId, tenantId);
            if (userOpt.isEmpty()) {
                log.warn("User {} not found in tenant {}", userId, tenantId);
                return false;
            }
            
            // Check if issue exists and belongs to tenant
            Optional<Issue> issueOpt = issueRepository.findByIdAndTenantId(issueId, tenantId);
            if (issueOpt.isEmpty()) {
                log.warn("Issue {} not found in tenant {}", issueId, tenantId);
                return false;
            }
            
            Issue issue = issueOpt.get();
            
            // User can access if they are:
            // 1. The assignee
            // 2. The reporter
            // 3. A watcher
            // 4. Admin of the tenant
            User user = userOpt.get();
            
            if (userId.equals(issue.getAssigneeId()) || 
                userId.equals(issue.getReporterId()) ||
                isUserWatcher(tenantId, userId, issueId) ||
                hasRole(user, "TENANT_ADMIN")) {
                return true;
            }
            
            // Check if user has access to the project
            return canAccessProject(tenantId, userId, issue.getProjectId());
            
        } catch (Exception e) {
            log.error("Error checking issue access for user {} to issue {}", userId, issueId, e);
            return false;
        }
    }
    
    /**
     * Check if user can modify an issue
     */
    public boolean canModifyIssue(String tenantId, String userId, String issueId) {
        try {
            // Check basic access first
            if (!canAccessIssue(tenantId, userId, issueId)) {
                return false;
            }
            
            Optional<User> userOpt = userRepository.findByIdAndTenantId(userId, tenantId);
            if (userOpt.isEmpty()) {
                return false;
            }
            
            User user = userOpt.get();
            
            // Admins can always modify
            if (hasRole(user, "TENANT_ADMIN")) {
                return true;
            }
            
            // Assignees can modify issues assigned to them
            Optional<Issue> issueOpt = issueRepository.findByIdAndTenantId(issueId, tenantId);
            if (issueOpt.isPresent()) {
                Issue issue = issueOpt.get();
                return userId.equals(issue.getAssigneeId());
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("Error checking issue modification access for user {} to issue {}", userId, issueId, e);
            return false;
        }
    }
    
    /**
     * Check if user can create issues in a project
     */
    public boolean canCreateIssue(String tenantId, String userId, String projectId) {
        try {
            // Check if user exists and belongs to tenant
            Optional<User> userOpt = userRepository.findByIdAndTenantId(userId, tenantId);
            if (userOpt.isEmpty()) {
                return false;
            }
            
            // All users can create issues in projects they have access to
            return canAccessProject(tenantId, userId, projectId);
            
        } catch (Exception e) {
            log.error("Error checking issue creation access for user {} in project {}", userId, projectId, e);
            return false;
        }
    }
    
    /**
     * Check if user can delete an issue
     */
    public boolean canDeleteIssue(String tenantId, String userId, String issueId) {
        try {
            Optional<User> userOpt = userRepository.findByIdAndTenantId(userId, tenantId);
            if (userOpt.isEmpty()) {
                return false;
            }
            
            User user = userOpt.get();
            
            // Only admins can delete issues
            return hasRole(user, "TENANT_ADMIN");
            
        } catch (Exception e) {
            log.error("Error checking issue deletion access for user {} to issue {}", userId, issueId, e);
            return false;
        }
    }
    
    /**
     * Check if user can access reports
     */
    public boolean canAccessReports(String tenantId, String userId) {
        try {
            Optional<User> userOpt = userRepository.findByIdAndTenantId(userId, tenantId);
            if (userOpt.isEmpty()) {
                return false;
            }
            
            // All users can access reports for their tenant
            return true;
            
        } catch (Exception e) {
            log.error("Error checking report access for user {} in tenant {}", userId, tenantId, e);
            return false;
        }
    }
    
    /**
     * Check if user can perform admin operations
     */
    public boolean canPerformAdminOperations(String tenantId, String userId) {
        try {
            Optional<User> userOpt = userRepository.findByIdAndTenantId(userId, tenantId);
            if (userOpt.isEmpty()) {
                return false;
            }
            
            User user = userOpt.get();
            return hasRole(user, "TENANT_ADMIN");
            
        } catch (Exception e) {
            log.error("Error checking admin access for user {} in tenant {}", userId, tenantId, e);
            return false;
        }
    }
    
    private boolean isUserWatcher(String tenantId, String userId, String issueId) {
        // TODO: Implement watcher check
        // This would query the watcher table
        return false;
    }
    
    private boolean hasRole(User user, String roleName) {
        return user.getRoles().stream()
            .anyMatch(role -> role.getName().equals(roleName));
    }
}
