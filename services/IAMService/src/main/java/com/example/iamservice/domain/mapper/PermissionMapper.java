package com.example.iamservice.domain.mapper;

import com.example.iamservice.domain.dto.response.PermissionResponse;
import com.example.iamservice.domain.dto.response.PermissionSummaryResponse;
import com.example.iamservice.domain.entity.Permission;
import org.springframework.stereotype.Component;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 15:43
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
public class PermissionMapper {

    public PermissionResponse toResponse(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getCode(),
                permission.getName(),
                permission.getDescription(),
                permission.getDeleted(),
                permission.getCreatedAt(),
                permission.getUpdatedAt()
        );
    }

    public PermissionSummaryResponse toSummaryResponse(Permission permission) {
        return new PermissionSummaryResponse(
                permission.getId(),
                permission.getCode(),
                permission.getName()
        );
    }
}
