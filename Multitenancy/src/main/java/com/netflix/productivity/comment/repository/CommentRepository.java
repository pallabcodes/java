package com.netflix.productivity.comment.repository;

import com.netflix.productivity.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, String> {
    Page<Comment> findByTenantIdAndIssueIdOrderByCreatedAtAsc(String tenantId, String issueId, Pageable pageable);
}


