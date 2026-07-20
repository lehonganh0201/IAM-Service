package com.example.iamservice.service;

import com.example.iamservice.domain.dto.response.common.PageResponse;
import com.example.iamservice.domain.dto.request.CreatePermissionRequest;
import com.example.iamservice.domain.dto.request.UpdatePermissionRequest;
import com.example.iamservice.domain.dto.response.PermissionResponse;
import org.springframework.data.domain.Pageable;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 15:45
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */


public interface PermissionManagementService {
    PageResponse<PermissionResponse> getPermissions(String keyword, Pageable pageable);

    PermissionResponse getPermissionById(Long id);

    PermissionResponse createPermission(CreatePermissionRequest request);

    PermissionResponse updatePermission(Long id, UpdatePermissionRequest request);

    void deletePermission(Long id, String reason);
}
