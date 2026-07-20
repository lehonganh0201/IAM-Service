package com.example.iamservice.repository.specification;

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
}
