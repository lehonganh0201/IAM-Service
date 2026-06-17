package com.example.iamservice.service.impl;

import com.example.iamservice.base.PageResponse;
import com.example.iamservice.domain.dto.request.CreatePermissionRequest;
import com.example.iamservice.domain.dto.request.UpdatePermissionRequest;
import com.example.iamservice.domain.dto.response.PermissionResponse;
import com.example.iamservice.domain.entity.Permission;
import com.example.iamservice.domain.mapper.PermissionMapper;
import com.example.iamservice.repository.PermissionRepository;
import com.example.iamservice.service.PermissionManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 15:49
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Service
@RequiredArgsConstructor
public class PermissionManagementServiceImpl implements PermissionManagementService {

    private static final Set<String> SYSTEM_PERMISSION_PREFIXES = Set.of(
            "USER_",
            "ROLE_",
            "PERMISSION_"
    );

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PermissionResponse> getPermissions(String keyword, Pageable pageable) {
        Page<Permission> permissions;

        if (StringUtils.hasText(keyword)) {
            permissions = permissionRepository
                    .findByDeletedFalseAndCodeContainingIgnoreCaseOrDeletedFalseAndNameContainingIgnoreCase(
                            keyword,
                            keyword,
                            pageable
                    );
        } else {
            permissions = permissionRepository.findByDeletedFalse(pageable);
        }

        Page<PermissionResponse> responsePage = permissions.map(permissionMapper::toResponse);

        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getPermissionById(Long id) {
        Permission permission = getActivePermission(id);

        return permissionMapper.toResponse(permission);
    }

    @Override
    @Transactional
    public PermissionResponse createPermission(CreatePermissionRequest request) {
        String normalizedCode = normalizeCode(request.getCode());

        if (permissionRepository.existsByCodeAndDeletedFalse(normalizedCode)) {
            throw new IllegalArgumentException("Permission code already exists");
        }

        Permission permission = Permission.builder()
                .code(normalizedCode)
                .name(request.getName())
                .description(request.getDescription())
                .deleted(false)
                .build();

        Permission savedPermission = permissionRepository.save(permission);

        return permissionMapper.toResponse(savedPermission);
    }

    @Override
    @Transactional
    public PermissionResponse updatePermission(Long id, UpdatePermissionRequest request) {
        Permission permission = getActivePermission(id);

        if (StringUtils.hasText(request.getCode())) {
            String normalizedCode = normalizeCode(request.getCode());

            if (!normalizedCode.equalsIgnoreCase(permission.getCode())
                    && permissionRepository.existsByCodeAndDeletedFalse(normalizedCode)) {
                throw new IllegalArgumentException("Permission code already exists");
            }

            validateSystemPermissionCodeChange(permission, normalizedCode);

            permission.setCode(normalizedCode);
        }

        if (StringUtils.hasText(request.getName())) {
            permission.setName(request.getName());
        }

        permission.setDescription(request.getDescription());

        Permission savedPermission = permissionRepository.save(permission);

        return permissionMapper.toResponse(savedPermission);
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        Permission permission = getActivePermission(id);

        if (isSystemPermission(permission)) {
            throw new IllegalArgumentException("System permission cannot be deleted");
        }

        permission.setDeleted(true);
        permissionRepository.save(permission);
    }

    private Permission getActivePermission(Long id) {
        return permissionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found"));
    }

    private String normalizeCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Code must not be null");
        }

        return code.trim().toUpperCase().replace(" ", "_");
    }

    private boolean isSystemPermission(Permission permission) {
        if (permission.getCode() == null) {
            return false;
        }

        return SYSTEM_PERMISSION_PREFIXES
                .stream()
                .anyMatch(prefix -> permission.getCode().startsWith(prefix));
    }

    private void validateSystemPermissionCodeChange(Permission permission, String newCode) {
        if (isSystemPermission(permission) && !permission.getCode().equals(newCode)) {
            throw new IllegalArgumentException("System permission code cannot be changed");
        }
    }
}
