package com.netflix.reporting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "report_metrics_agg",
       uniqueConstraints = @UniqueConstraint(columnNames = {
           "tenant_id", "project_id", "metric_type", "metric_name", 
           "aggregation_period", "period_start"
       }))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportMetricsAgg {
    
    @Id
    @Column(length = 36)
    private String id;
    
    @Column(name = "tenant_id", length = 36, nullable = false)
    private String tenantId;
    
    @Column(name = "project_id", length = 36)
    private String projectId;
    
    @Column(name = "metric_type", length = 50, nullable = false)
    private String metricType;
    
    @Column(name = "metric_name", length = 100, nullable = false)
    private String metricName;
    
    @Column(name = "metric_value", precision = 15, scale = 4, nullable = false)
    private BigDecimal metricValue;
    
    @Column(name = "aggregation_period", length = 20, nullable = false)
    private String aggregationPeriod;
    
    @Column(name = "period_start", nullable = false)
    private OffsetDateTime periodStart;
    
    @Column(name = "period_end", nullable = false)
    private OffsetDateTime periodEnd;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
