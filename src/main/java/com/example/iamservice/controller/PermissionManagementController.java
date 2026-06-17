package com.example.iamservice.controller;

import com.example.iamservice.base.PageResponse;
import com.example.iamservice.domain.dto.request.CreatePermissionRequest;
import com.example.iamservice.domain.dto.request.UpdatePermissionRequest;
import com.example.iamservice.domain.dto.response.PermissionResponse;
import com.example.iamservice.service.PermissionManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    private final PermissionManagementService permissionManagementService;

    @PreAuthorize("hasPermission(null, 'PERMISSION_READ')")
    @GetMapping
    public ResponseEntity<PageResponse<PermissionResponse>> getPermissions(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(permissionManagementService.getPermissions(keyword, pageable));
    }

    @PreAuthorize("hasPermission(null, 'PERMISSION_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<PermissionResponse> getPermissionById(@PathVariable Long id) {
        return ResponseEntity.ok(permissionManagementService.getPermissionById(id));
    }

    @PreAuthorize("hasPermission(null, 'PERMISSION_CREATE')")
    @PostMapping
    public ResponseEntity<PermissionResponse> createPermission(
            @Valid @RequestBody CreatePermissionRequest request
    ) {
        PermissionResponse response = permissionManagementService.createPermission(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasPermission(null, 'PERMISSION_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<PermissionResponse> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePermissionRequest request
    ) {
        return ResponseEntity.ok(permissionManagementService.updatePermission(id, request));
    }

    @PreAuthorize("hasPermission(null, 'PERMISSION_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        permissionManagementService.deletePermission(id);

        return ResponseEntity.noContent().build();
    }
}
