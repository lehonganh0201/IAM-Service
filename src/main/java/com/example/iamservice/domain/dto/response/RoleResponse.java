package com.example.iamservice.domain.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 15:43
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */


public record RoleResponse(
        Long id,
        String code,
        String name,
        String description,
        Boolean deleted,
        Set<PermissionSummaryResponse> permissions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
