package com.rroms.restaurantmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public abstract class BaseEntity {
    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
//        this.createdBy = SecurityUtil.getCurrentUsername();
        this.createdAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
//        this.updatedBy = SecurityUtil.getCurrentUsername();
        this.updatedAt = Instant.now();
    }
}
