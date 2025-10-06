package com.netflix.reporting.service;

import com.netflix.reporting.entity.ReportMetricsAgg;
import com.netflix.reporting.repository.ReportMetricsAggRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportMetricsService {
    
    private final ReportMetricsAggRepository reportMetricsAggRepository;
    
    @Transactional
    public void aggregateMetrics(String tenantId, String projectId, 
                                String metricType, String metricName, 
                                BigDecimal metricValue, 
                                String aggregationPeriod,
                                OffsetDateTime periodStart, 
                                OffsetDateTime periodEnd) {
        
        ReportMetricsAgg existing = reportMetricsAggRepository.findByTenantIdAndProjectIdAndMetricTypeAndMetricNameAndAggregationPeriodAndPeriodStart(
            tenantId, projectId, metricType, metricName, aggregationPeriod, periodStart);
        
        if (existing != null) {
            existing.setMetricValue(metricValue);
            existing.setUpdatedAt(OffsetDateTime.now());
            reportMetricsAggRepository.save(existing);
        } else {
            ReportMetricsAgg newMetric = ReportMetricsAgg.builder()
                .id(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .projectId(projectId)
                .metricType(metricType)
                .metricName(metricName)
                .metricValue(metricValue)
                .aggregationPeriod(aggregationPeriod)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .build();
            
            reportMetricsAggRepository.save(newMetric);
        }
    }
    
    @Transactional(readOnly = true)
    public List<ReportMetricsAgg> getMetricsByPeriod(String tenantId, String projectId, 
                                                     String metricType, 
                                                     OffsetDateTime fromDate, 
                                                     OffsetDateTime toDate) {
        return reportMetricsAggRepository.findMetricsByPeriod(tenantId, projectId, metricType, fromDate, toDate);
    }
    
    @Transactional(readOnly = true)
    public List<ReportMetricsAgg> getMetricsByAggregationPeriod(String tenantId, 
                                                                String aggregationPeriod,
                                                                OffsetDateTime fromDate, 
                                                                OffsetDateTime toDate) {
        return reportMetricsAggRepository.findMetricsByAggregationPeriod(tenantId, aggregationPeriod, fromDate, toDate);
    }
}
