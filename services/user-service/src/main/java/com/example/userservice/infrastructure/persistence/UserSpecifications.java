package com.example.userservice.infrastructure.persistence;

import com.example.userservice.application.dto.request.UserSearchQuery;
import com.example.userservice.domain.model.UserStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 15:34
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public final class UserSpecifications {
    private UserSpecifications() {
    }

    public static Specification<UserEntity> byQuery(UserSearchQuery q) {
        return (root, cq, cb) -> {
            var p = cb.conjunction();
            p = cb.and(p, cb.notEqual(root.get("status"), UserStatus.DELETED));
            if (StringUtils.hasText(q.keyword())) {
                String like = "%" + q.keyword().toLowerCase() + "%";
                p = cb.and(p, cb.or(cb.like(cb.lower(root.get("username")), like), cb.like(cb.lower(root.get("fullName")), like)));
            }
            if (StringUtils.hasText(q.province()))
                p = cb.and(p, cb.equal(cb.lower(root.get("province")), q.province().toLowerCase()));
            if (q.minYears() != null)
                p = cb.and(p, cb.greaterThanOrEqualTo(root.get("yearsOfExperience"), q.minYears()));
            if (q.maxYears() != null) p = cb.and(p, cb.lessThanOrEqualTo(root.get("yearsOfExperience"), q.maxYears()));
            return p;
        };
    }
}

