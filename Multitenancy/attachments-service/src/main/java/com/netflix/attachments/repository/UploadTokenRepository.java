package com.netflix.attachments.repository;

import com.netflix.attachments.entity.UploadToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface UploadTokenRepository extends JpaRepository<UploadToken, String> {
    
    @Query("SELECT t FROM UploadToken t WHERE t.id = :id AND t.tenantId = :tenantId AND t.expiresAt > :now")
    Optional<UploadToken> findValid(@Param("id") String id, 
                                    @Param("tenantId") String tenantId, 
                                    @Param("now") OffsetDateTime now);

    @Modifying
    @Query("DELETE FROM UploadToken t WHERE t.expiresAt < :now")
    int deleteExpired(@Param("now") OffsetDateTime now);
}
