package com.example.iamservice.repository.specification;

import com.example.iamservice.constant.AuditAction;
import com.example.iamservice.constant.AuditResult;
import com.example.iamservice.domain.entity.AuditLog;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.Instant;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    18/06/2026 at 9:27
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public final class AuditLogSpecification {

    private AuditLogSpecification() {
    }

    public static Specification<AuditLog> actorUserIdEquals(Long actorUserId) {
        return (root, query, cb) -> {
            if (actorUserId == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("actorUserId"), actorUserId);
        };
    }

    public static Specification<AuditLog> actionEquals(String action) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(action)) {
                return cb.conjunction();
            }

            return cb.equal(root.get("action"), AuditAction.valueOf(action));
        };
    }

    public static Specification<AuditLog> resultEquals(String result) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(result)) {
                return cb.conjunction();
            }

            return cb.equal(root.get("result"), AuditResult.valueOf(result));
        };
    }

    public static Specification<AuditLog> keywordContains(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) {
                return cb.conjunction();
            }

            String likeKeyword = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("actorUsername")), likeKeyword),
                    cb.like(cb.lower(root.get("actorEmail")), likeKeyword),
                    cb.like(cb.lower(root.get("requestPath")), likeKeyword),
                    cb.like(cb.lower(root.get("requestId")), likeKeyword),
                    cb.like(cb.lower(root.get("message")), likeKeyword)
            );
        };
    }

    public static Specification<AuditLog> createdAtFrom(Instant from) {
        return (root, query, cb) -> {
            if (from == null) {
                return cb.conjunction();
            }

            return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
        };
    }

    public static Specification<AuditLog> createdAtTo(Instant to) {
        return (root, query, cb) -> {
            if (to == null) {
                return cb.conjunction();
            }

            return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }
}