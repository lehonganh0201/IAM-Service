package com.example.iamservice.service;

import com.example.commonlib.api.common.PageResponse;
import com.example.iamservice.domain.dto.request.AssignRolePermissionsRequest;
import com.example.iamservice.domain.dto.request.CreateRoleRequest;
import com.example.iamservice.domain.dto.request.UpdateRoleRequest;
import com.example.iamservice.domain.dto.response.RoleResponse;
import org.springframework.data.domain.Pageable;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 15:45
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */


public interface RoleManagementService {
    PageResponse<RoleResponse> getRoles(String keyword, Pageable pageable);

    RoleResponse getRoleById(Long id);

    RoleResponse createRole(CreateRoleRequest request);

    RoleResponse updateRole(Long id, UpdateRoleRequest request);

    RoleResponse assignPermissions(Long id, AssignRolePermissionsRequest request);

    void deleteRole(Long id, String reason);
}
