package com.example.iamservice.controller;

import com.example.iamservice.aop.annotation.AuditActivity;
import com.example.iamservice.constant.AuditAction;
import com.example.iamservice.constant.AuditResourceType;
import com.example.iamservice.domain.dto.request.CreatePermissionRequest;
import com.example.iamservice.domain.dto.request.DeleteReasonRequest;
import com.example.iamservice.domain.dto.request.UpdatePermissionRequest;
import com.example.iamservice.domain.dto.response.PermissionResponse;
import com.example.iamservice.domain.dto.response.common.ApiResponse;
import com.example.iamservice.domain.dto.response.common.ApiResponseFactory;
import com.example.iamservice.domain.dto.response.common.PageResponse;
import com.example.iamservice.domain.dto.response.common.PageableFactory;
import com.example.iamservice.service.PermissionManagementService;
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
 * Created on:    17/06/2026 at 15:51
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@RestController
@RequestMapping("/api/v1/admin/permissions")
@RequiredArgsConstructor
public class PermissionManagementController {

    private static final Set<String> PERMISSION_SORT_FIELDS = Set.of(
            "id",
            "code",
            "name",
            "createdAt",
            "updatedAt"
    );

    private final PermissionManagementService permissionManagementService;
    private final ApiResponseFactory responseFactory;
    private final PageableFactory pageableFactory;

    @PreAuthorize("hasPermission(null, 'PERMISSION_READ')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponse>>> getPermissions(
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
                PERMISSION_SORT_FIELDS
        );

        PageResponse<PermissionResponse> data =
                permissionManagementService.getPermissions(keyword, pageable);

        return ResponseEntity.ok(
                responseFactory.success("Get permissions successfully", data)
        );
    }

    @PreAuthorize("hasPermission(null, 'PERMISSION_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionById(@PathVariable Long id) {
        PermissionResponse data = permissionManagementService.getPermissionById(id);

        return ResponseEntity.ok(
                responseFactory.success("Get permission successfully", data)
        );
    }

    @AuditActivity(
            action = AuditAction.PERMISSION_CREATE,
            resourceType = AuditResourceType.PERMISSION,
            message = "Create permission"
    )
    @PreAuthorize("hasPermission(null, 'PERMISSION_CREATE')")
    @PostMapping
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
            @Valid @RequestBody CreatePermissionRequest request
    ) {
        PermissionResponse data = permissionManagementService.createPermission(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseFactory.success("Create permission successfully", data));
    }

    @AuditActivity(
            action = AuditAction.PERMISSION_UPDATE,
            resourceType = AuditResourceType.PERMISSION,
            resourceIdParam = "id",
            message = "Update permission"
    )
    @PreAuthorize("hasPermission(null, 'PERMISSION_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePermissionRequest request
    ) {
        PermissionResponse data = permissionManagementService.updatePermission(id, request);

        return ResponseEntity.ok(
                responseFactory.success("Update permission successfully", data)
        );
    }

    @AuditActivity(
            action = AuditAction.PERMISSION_DELETE,
            resourceType = AuditResourceType.PERMISSION,
            resourceIdParam = "id",
            message = "Delete permission"
    )
    @PreAuthorize("hasPermission(null, 'PERMISSION_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePermission(
            @PathVariable Long id,
            @RequestBody(required = false) DeleteReasonRequest request) {
        String reason = request == null ? null : request.getReason();

        permissionManagementService.deletePermission(id, reason);

        return ResponseEntity.ok(
                responseFactory.success("Delete permission successfully")
        );
    }
}
