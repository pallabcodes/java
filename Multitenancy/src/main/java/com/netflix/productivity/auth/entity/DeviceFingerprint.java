package com.netflix.productivity.auth.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "device_fingerprints",
        indexes = {
                @Index(name = "idx_df_user", columnList = "tenant_id,user_id,hash")
        })
public class DeviceFingerprint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;
    @Column(name = "hash", nullable = false, length = 255)
    private String hash;
    @Column(name = "label", length = 255)
    private String label;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}


