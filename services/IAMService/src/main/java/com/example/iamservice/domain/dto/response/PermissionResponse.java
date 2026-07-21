package com.example.iamservice.domain.dto.response;

import java.time.LocalDateTime;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 15:42
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */


public record PermissionResponse(
        Long id,
        String code,
        String name,
        String description,
        Boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
