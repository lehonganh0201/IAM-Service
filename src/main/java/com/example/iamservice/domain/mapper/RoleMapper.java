package com.example.iamservice.domain.mapper;

import com.example.iamservice.domain.dto.response.PermissionSummaryResponse;
import com.example.iamservice.domain.dto.response.RoleResponse;
import com.example.iamservice.domain.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 15:44
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
@RequiredArgsConstructor
public class RoleMapper {

    private final PermissionMapper permissionMapper;

    public RoleResponse toResponse(Role role) {
        Set<PermissionSummaryResponse> permissions = role.getPermissions() == null
                ? Set.of()
                : role.getPermissions()
                .stream()
                .filter(permission -> !Boolean.TRUE.equals(permission.getDeleted()))
                .map(permissionMapper::toSummaryResponse)
                .collect(Collectors.toSet());

        return new RoleResponse(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.getDeleted(),
                permissions,
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }
}
