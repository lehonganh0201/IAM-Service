package com.example.iamservice.repository.specification;

import com.example.iamservice.domain.dto.request.UserSearchQuery;
import com.example.iamservice.domain.entity.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 16:29
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public final class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> notDeleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isFalse(root.get("deleted"));
    }

    public static Specification<User> keywordContains(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(keyword)) {
                return criteriaBuilder.conjunction();
            }

            String likeKeyword = "%" + keyword.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), likeKeyword),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likeKeyword),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), likeKeyword),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), likeKeyword)
            );
        };
    }

    public static Specification<User> byQuery(UserSearchQuery q) {
        return (root, cq, cb) -> {
            var p = cb.conjunction();
            p = cb.and(p, cb.notEqual(root.get("enabled"), Boolean.FALSE));
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
