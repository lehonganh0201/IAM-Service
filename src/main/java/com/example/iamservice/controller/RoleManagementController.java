package com.example.iamservice.controller;

import com.example.iamservice.domain.dto.request.AssignRolePermissionsRequest;
import com.example.iamservice.domain.dto.request.CreateRoleRequest;
import com.example.iamservice.domain.dto.request.DeleteReasonRequest;
import com.example.iamservice.domain.dto.request.UpdateRoleRequest;
import com.example.iamservice.domain.dto.response.RoleResponse;
import com.example.iamservice.domain.dto.response.common.ApiResponse;
import com.example.iamservice.domain.dto.response.common.ApiResponseFactory;
import com.example.iamservice.domain.dto.response.common.PageResponse;
import com.example.iamservice.domain.dto.response.common.PageableFactory;
import com.example.iamservice.service.RoleManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 15:50
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
public class RoleManagementController {

    private static final Set<String> ROLE_SORT_FIELDS = Set.of(
            "id",
            "code",
            "name",
            "createdAt",
            "updatedAt"
    );

    private final RoleManagementService roleManagementService;
    private final ApiResponseFactory responseFactory;
    private final PageableFactory pageableFactory;

    @PreAuthorize("hasPermission(null, 'ROLE_READ')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RoleResponse>>> getRoles(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Pageable pageable = pageableFactory.create(
                page,
                size,
                sortBy,
                sortDir,
                ROLE_SORT_FIELDS
        );

        PageResponse<RoleResponse> data =
                roleManagementService.getRoles(keyword, pageable);

        return ResponseEntity.ok(
                responseFactory.success("Get roles successfully", data)
        );
    }

    @PreAuthorize("hasPermission(null, 'ROLE_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long id) {
        RoleResponse data = roleManagementService.getRoleById(id);

        return ResponseEntity.ok(
                responseFactory.success("Get role successfully", data)
        );
    }

    @PreAuthorize("hasPermission(null, 'ROLE_CREATE')")
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Valid @RequestBody CreateRoleRequest request
    ) {
        RoleResponse data = roleManagementService.createRole(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseFactory.success("Create role successfully", data));
    }

    @PreAuthorize("hasPermission(null, 'ROLE_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        RoleResponse data = roleManagementService.updateRole(id, request);

        return ResponseEntity.ok(
                responseFactory.success("Update role successfully", data)
        );
    }

    @PreAuthorize("hasPermission(null, 'ROLE_ASSIGN_PERMISSION')")
    @PatchMapping("/{id}/permissions")
    public ResponseEntity<ApiResponse<RoleResponse>> assignPermissions(
            @PathVariable Long id,
            @Valid @RequestBody AssignRolePermissionsRequest request
    ) {
        RoleResponse data = roleManagementService.assignPermissions(id, request);

        return ResponseEntity.ok(
                responseFactory.success("Assign role permissions successfully", data)
        );
    }

    @PreAuthorize("hasPermission(null, 'ROLE_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(
            @PathVariable Long id,
            @RequestBody(required = false) DeleteReasonRequest request
    ) {
        String reason = request == null ? null : request.getReason();

        roleManagementService.deleteRole(id, reason);

        return ResponseEntity.ok(
                responseFactory.success("Delete role successfully")
        );
    }
}