package com.example.iamservice.service.impl;

import com.example.iamservice.domain.dto.response.common.PageResponse;
import com.example.iamservice.domain.dto.request.AssignRolePermissionsRequest;
import com.example.iamservice.domain.dto.request.CreateRoleRequest;
import com.example.iamservice.domain.dto.request.UpdateRoleRequest;
import com.example.iamservice.domain.dto.response.RoleResponse;
import com.example.iamservice.domain.entity.Permission;
import com.example.iamservice.domain.entity.Role;
import com.example.iamservice.domain.mapper.RoleMapper;
import com.example.iamservice.exception.BadRequestException;
import com.example.iamservice.exception.ConflictException;
import com.example.iamservice.exception.NotFoundException;
import com.example.iamservice.repository.PermissionRepository;
import com.example.iamservice.repository.RoleRepository;
import com.example.iamservice.repository.specification.RoleSpecification;
import com.example.iamservice.service.RoleManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 15:45
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Service
@RequiredArgsConstructor
public class RoleManagementServiceImpl implements RoleManagementService {

    private static final Set<String> SYSTEM_ROLE_CODES = Set.of(
            "USER_MANAGER",
            "SYSTEM_ADMIN"
    );

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;
    private final SoftDeleteService softDeleteService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoleResponse> getRoles(String keyword, Pageable pageable) {
        Specification<Role> specification = Specification
                .where(RoleSpecification.notDeleted())
                .and(RoleSpecification.keywordContains(keyword));

        Page<Role> roles = roleRepository.findAll(specification, pageable);

        Page<RoleResponse> responsePage = roles.map(roleMapper::toResponse);

        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) {
        Role role = getActiveRole(id);

        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        String normalizedCode = normalizeCode(request.getCode());

        if (roleRepository.existsByCodeAndDeletedFalse(normalizedCode)) {
            throw new ConflictException("Role code already exists");
        }

        Role role = Role.builder()
                .code(normalizedCode)
                .name(request.getName())
                .description(request.getDescription())
                .permissions(resolvePermissions(request.getPermissionCodes()))
                .build();

        Role savedRole = roleRepository.save(role);

        return roleMapper.toResponse(savedRole);
    }

    @Override
    @Transactional
    public RoleResponse updateRole(Long id, UpdateRoleRequest request) {
        Role role = getActiveRole(id);

        if (StringUtils.hasText(request.getCode())) {
            String normalizedCode = normalizeCode(request.getCode());

            if (!normalizedCode.equalsIgnoreCase(role.getCode())
                    && roleRepository.existsByCodeAndDeletedFalse(normalizedCode)) {
                throw new ConflictException("Role code already exists");
            }

            validateSystemRoleCodeChange(role, normalizedCode);

            role.setCode(normalizedCode);
        }

        if (StringUtils.hasText(request.getName())) {
            role.setName(request.getName());
        }

        role.setDescription(request.getDescription());

        Role savedRole = roleRepository.save(role);

        return roleMapper.toResponse(savedRole);
    }

    @Override
    @Transactional
    public RoleResponse assignPermissions(Long id, AssignRolePermissionsRequest request) {
        Role role = getActiveRole(id);

        Set<Permission> permissions = resolvePermissions(request.getPermissionCodes());

        role.setPermissions(permissions);

        Role savedRole = roleRepository.save(role);

        return roleMapper.toResponse(savedRole);
    }

    @Override
    @Transactional
    public void deleteRole(Long id, String reason) {
        Role role = getActiveRole(id);

        if (isSystemRole(role)) {
            throw new ConflictException("System role cannot be deleted");
        }

        softDeleteService.markDeleted(role, reason);
        roleRepository.save(role);
    }

    private Role getActiveRole(Long id) {
        return roleRepository.findWithPermissionsByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Role not found"));
    }

    private Set<Permission> resolvePermissions(Set<String> permissionCodes) {
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return Set.of();
        }

        Set<String> normalizedCodes = permissionCodes
                .stream()
                .map(this::normalizeCode)
                .collect(java.util.stream.Collectors.toSet());

        List<Permission> permissions =
                permissionRepository.findByCodeInAndDeletedFalse(normalizedCodes);

        if (permissions.size() != normalizedCodes.size()) {
            throw new NotFoundException("Some permissions do not exist or have been deleted");
        }

        return new HashSet<>(permissions);
    }

    private String normalizeCode(String code) {
        if (code == null) {
            throw new BadRequestException("Code must not be null");
        }

        return code.trim().toUpperCase().replace(" ", "_");
    }

    private boolean isSystemRole(Role role) {
        return role.getCode() != null && SYSTEM_ROLE_CODES.contains(role.getCode());
    }

    private void validateSystemRoleCodeChange(Role role, String newCode) {
        if (isSystemRole(role) && !role.getCode().equals(newCode)) {
            throw new BadRequestException("System role code cannot be changed");
        }
    }
}
