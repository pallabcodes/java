package com.netflix.productivity.security;

import com.netflix.productivity.entity.Issue;
import com.netflix.productivity.entity.Project;
import com.netflix.productivity.entity.Role;
import com.netflix.productivity.entity.User;
import com.netflix.productivity.repository.IssueRepository;
import com.netflix.productivity.repository.ProjectRepository;
import com.netflix.productivity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationPolicyTest {
    
    @Mock
    private IssueRepository issueRepository;
    
    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private AuthorizationPolicy authorizationPolicy;
    
    private String tenantId;
    private String userId;
    private String projectId;
    private String issueId;
    private User user;
    private User adminUser;
    private Project project;
    private Issue issue;
    
    @BeforeEach
    void setUp() {
        tenantId = "tenant_123";
        userId = "user_123";
        projectId = "project_123";
        issueId = "issue_123";
        
        // Create regular user
        user = new User();
        user.setId(userId);
        user.setTenantId(tenantId);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        
        Role userRole = new Role();
        userRole.setName("TENANT_USER");
        user.setRoles(Set.of(userRole));
        
        // Create admin user
        adminUser = new User();
        adminUser.setId("admin_123");
        adminUser.setTenantId(tenantId);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        
        Role adminRole = new Role();
        adminRole.setName("TENANT_ADMIN");
        adminUser.setRoles(Set.of(adminRole));
        
        // Create project
        project = new Project();
        project.setId(projectId);
        project.setTenantId(tenantId);
        project.setName("Test Project");
        
        // Create issue
        issue = new Issue();
        issue.setId(issueId);
        issue.setTenantId(tenantId);
        issue.setProjectId(projectId);
        issue.setTitle("Test Issue");
        issue.setAssigneeId(userId);
        issue.setReporterId(userId);
    }
    
    @Test
    void canAccessProject_ValidUserAndProject_ReturnsTrue() {
        // Given
        when(userRepository.findByIdAndTenantId(userId, tenantId))
            .thenReturn(Optional.of(user));
        when(projectRepository.findByIdAndTenantId(projectId, tenantId))
            .thenReturn(Optional.of(project));
        
        // When
        boolean result = authorizationPolicy.canAccessProject(tenantId, userId, projectId);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void canAccessProject_UserNotFound_ReturnsFalse() {
        // Given
        when(userRepository.findByIdAndTenantId(userId, tenantId))
            .thenReturn(Optional.empty());
        
        // When
        boolean result = authorizationPolicy.canAccessProject(tenantId, userId, projectId);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void canAccessProject_ProjectNotFound_ReturnsFalse() {
        // Given
        when(userRepository.findByIdAndTenantId(userId, tenantId))
            .thenReturn(Optional.of(user));
        when(projectRepository.findByIdAndTenantId(projectId, tenantId))
            .thenReturn(Optional.empty());
        
        // When
        boolean result = authorizationPolicy.canAccessProject(tenantId, userId, projectId);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void canAccessIssue_Assignee_ReturnsTrue() {
        // Given
        when(userRepository.findByIdAndTenantId(userId, tenantId))
            .thenReturn(Optional.of(user));
        when(issueRepository.findByIdAndTenantId(issueId, tenantId))
            .thenReturn(Optional.of(issue));
        when(projectRepository.findByIdAndTenantId(projectId, tenantId))
            .thenReturn(Optional.of(project));
        
        // When
        boolean result = authorizationPolicy.canAccessIssue(tenantId, userId, issueId);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void canAccessIssue_Reporter_ReturnsTrue() {
        // Given
        issue.setAssigneeId("other_user");
        when(userRepository.findByIdAndTenantId(userId, tenantId))
            .thenReturn(Optional.of(user));
        when(issueRepository.findByIdAndTenantId(issueId, tenantId))
            .thenReturn(Optional.of(issue));
        when(projectRepository.findByIdAndTenantId(projectId, tenantId))
            .thenReturn(Optional.of(project));
        
        // When
        boolean result = authorizationPolicy.canAccessIssue(tenantId, userId, issueId);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void canModifyIssue_Assignee_ReturnsTrue() {
        // Given
        when(userRepository.findByIdAndTenantId(userId, tenantId))
            .thenReturn(Optional.of(user));
        when(issueRepository.findByIdAndTenantId(issueId, tenantId))
            .thenReturn(Optional.of(issue));
        when(projectRepository.findByIdAndTenantId(projectId, tenantId))
            .thenReturn(Optional.of(project));
        
        // When
        boolean result = authorizationPolicy.canModifyIssue(tenantId, userId, issueId);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void canModifyIssue_Admin_ReturnsTrue() {
        // Given
        issue.setAssigneeId("other_user");
        when(userRepository.findByIdAndTenantId("admin_123", tenantId))
            .thenReturn(Optional.of(adminUser));
        when(issueRepository.findByIdAndTenantId(issueId, tenantId))
            .thenReturn(Optional.of(issue));
        when(projectRepository.findByIdAndTenantId(projectId, tenantId))
            .thenReturn(Optional.of(project));
        
        // When
        boolean result = authorizationPolicy.canModifyIssue(tenantId, "admin_123", issueId);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void canModifyIssue_NonAssigneeNonAdmin_ReturnsFalse() {
        // Given
        issue.setAssigneeId("other_user");
        when(userRepository.findByIdAndTenantId(userId, tenantId))
            .thenReturn(Optional.of(user));
        when(issueRepository.findByIdAndTenantId(issueId, tenantId))
            .thenReturn(Optional.of(issue));
        when(projectRepository.findByIdAndTenantId(projectId, tenantId))
            .thenReturn(Optional.of(project));
        
        // When
        boolean result = authorizationPolicy.canModifyIssue(tenantId, userId, issueId);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void canCreateIssue_ValidUserAndProject_ReturnsTrue() {
        // Given
        when(userRepository.findByIdAndTenantId(userId, tenantId))
            .thenReturn(Optional.of(user));
        when(projectRepository.findByIdAndTenantId(projectId, tenantId))
            .thenReturn(Optional.of(project));
        
        // When
        boolean result = authorizationPolicy.canCreateIssue(tenantId, userId, projectId);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void canDeleteIssue_Admin_ReturnsTrue() {
        // Given
        when(userRepository.findByIdAndTenantId("admin_123", tenantId))
            .thenReturn(Optional.of(adminUser));
        
        // When
        boolean result = authorizationPolicy.canDeleteIssue(tenantId, "admin_123", issueId);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void canDeleteIssue_NonAdmin_ReturnsFalse() {
        // Given
        when(userRepository.findByIdAndTenantId(userId, tenantId))
            .thenReturn(Optional.of(user));
        
        // When
        boolean result = authorizationPolicy.canDeleteIssue(tenantId, userId, issueId);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void canAccessReports_ValidUser_ReturnsTrue() {
        // Given
        when(userRepository.findByIdAndTenantId(userId, tenantId))
            .thenReturn(Optional.of(user));
        
        // When
        boolean result = authorizationPolicy.canAccessReports(tenantId, userId);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void canPerformAdminOperations_Admin_ReturnsTrue() {
        // Given
        when(userRepository.findByIdAndTenantId("admin_123", tenantId))
            .thenReturn(Optional.of(adminUser));
        
        // When
        boolean result = authorizationPolicy.canPerformAdminOperations(tenantId, "admin_123");
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void canPerformAdminOperations_NonAdmin_ReturnsFalse() {
        // Given
        when(userRepository.findByIdAndTenantId(userId, tenantId))
            .thenReturn(Optional.of(user));
        
        // When
        boolean result = authorizationPolicy.canPerformAdminOperations(tenantId, userId);
        
        // Then
        assertFalse(result);
    }
}
