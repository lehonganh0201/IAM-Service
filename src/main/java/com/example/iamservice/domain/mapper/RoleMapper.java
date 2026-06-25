package com.example.iamservice.domain.mapper;

import com.example.iamservice.domain.dto.response.PermissionSummaryResponse;
import com.example.iamservice.domain.dto.response.RoleResponse;
import com.example.iamservice.domain.entity.Role;
import com.example.iamservice.repository.PermissionRepository;
import com.example.iamservice.repository.RolePermissionRepository;
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
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    public RoleResponse toResponse(Role role) {

        Set<Long> permissionIds = rolePermissionRepository
                .findPermissionIdsByRoleId(role.getId());

        Set<PermissionSummaryResponse> permissions =
                permissionIds == null ? Set.of()
                        : permissionRepository.findAllById(permissionIds)
                        .stream()
                        .filter(p -> !Boolean.TRUE.equals(p.getDeleted()))
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
