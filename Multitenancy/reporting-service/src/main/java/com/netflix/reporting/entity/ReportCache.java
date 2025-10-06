package com.netflix.reporting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "report_cache", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "cache_key"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCache {
    
    @Id
    @Column(length = 36)
    private String id;
    
    @Column(name = "tenant_id", length = 36, nullable = false)
    private String tenantId;
    
    @Column(name = "cache_key", length = 255, nullable = false)
    private String cacheKey;
    
    @Column(name = "report_data", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private String reportData;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
    
    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;
    
    @Column(name = "created_by", length = 36, nullable = false)
    private String createdBy;
}
