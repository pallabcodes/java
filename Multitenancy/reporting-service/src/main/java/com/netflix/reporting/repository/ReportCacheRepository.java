package com.netflix.reporting.repository;

import com.netflix.reporting.entity.ReportCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportCacheRepository extends JpaRepository<ReportCache, String> {
    
    Optional<ReportCache> findByTenantIdAndCacheKey(String tenantId, String cacheKey);
    
    @Query("SELECT rc FROM ReportCache rc WHERE rc.tenantId = :tenantId AND rc.cacheKey = :cacheKey AND rc.expiresAt > :now")
    Optional<ReportCache> findValidCache(@Param("tenantId") String tenantId, 
                                        @Param("cacheKey") String cacheKey, 
                                        @Param("now") OffsetDateTime now);
    
    @Modifying
    @Query("DELETE FROM ReportCache rc WHERE rc.expiresAt < :now")
    int deleteExpiredCaches(@Param("now") OffsetDateTime now);
    
    @Modifying
    @Query("DELETE FROM ReportCache rc WHERE rc.tenantId = :tenantId")
    int deleteByTenantId(@Param("tenantId") String tenantId);
    
    List<ReportCache> findByTenantId(String tenantId);
}
