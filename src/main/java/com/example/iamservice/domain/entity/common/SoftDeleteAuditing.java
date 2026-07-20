package com.example.iamservice.domain.entity.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 16:53
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Setter
@Getter
public abstract class SoftDeleteAuditing extends UserDateAuditing {

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "deletion_reason", length = 500)
    private String deletionReason;

    public void markDeleted(Long deletedBy, String reason) {
        this.deleted = true;
        this.deletedAt = Instant.now();
        this.deletedBy = deletedBy;
        this.deletionReason = reason;
    }

    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
        this.deletionReason = null;
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.deleted);
    }
}