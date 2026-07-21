package com.example.storageservice.infrastructure.persistence;

import com.example.storageservice.application.dto.request.FileSearchQuery;
import com.example.storageservice.domain.model.FileStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 11:39
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public final class FileMetadataSpecifications {
    private FileMetadataSpecifications() {
    }

    public static Specification<FileMetadataEntity> byQuery(FileSearchQuery q) {
        return (root, cq, cb) -> {
            var p = cb.conjunction();
            p = cb.and(p, cb.equal(root.get("status"), FileStatus.ACTIVE), cb.equal(root.get("visibility"), q.visibility()));
            if (StringUtils.hasText(q.keyword())) {
                String like = "%" + q.keyword().toLowerCase() + "%";
                p = cb.and(p, cb.like(cb.lower(root.get("originalName")), like));
            }
            if (StringUtils.hasText(q.contentType())) p = cb.and(p, cb.equal(root.get("contentType"), q.contentType()));
            if (StringUtils.hasText(q.extension()))
                p = cb.and(p, cb.equal(cb.lower(root.get("extension")), q.extension().toLowerCase()));
            if (StringUtils.hasText(q.ownerId())) p = cb.and(p, cb.equal(root.get("ownerId"), q.ownerId()));
            if (q.fromDate() != null) p = cb.and(p, cb.greaterThanOrEqualTo(root.get("createdAt"), q.fromDate()));
            if (q.toDate() != null) p = cb.and(p, cb.lessThanOrEqualTo(root.get("createdAt"), q.toDate()));
            return p;
        };
    }
}

