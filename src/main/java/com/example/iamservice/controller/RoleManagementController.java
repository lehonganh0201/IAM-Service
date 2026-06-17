package com.example.iamservice.controller;

import com.example.iamservice.base.PageResponse;
import com.example.iamservice.domain.dto.request.AssignRolePermissionsRequest;
import com.example.iamservice.domain.dto.request.CreateRoleRequest;
import com.example.iamservice.domain.dto.request.UpdateRoleRequest;
import com.example.iamservice.domain.dto.response.RoleResponse;
import com.example.iamservice.service.RoleManagementService;
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
 * Created on:    17/06/2026 at 15:50
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
public class RoleManagementController {

    private final RoleManagementService roleManagementService;

    @PreAuthorize("hasPermission(null, 'ROLE_READ')")
    @GetMapping
    public ResponseEntity<PageResponse<RoleResponse>> getRoles(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(roleManagementService.getRoles(keyword, pageable));
    }

    @PreAuthorize("hasPermission(null, 'ROLE_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleManagementService.getRoleById(id));
    }

    @PreAuthorize("hasPermission(null, 'ROLE_CREATE')")
    @PostMapping
    public ResponseEntity<RoleResponse> createRole(
            @Valid @RequestBody CreateRoleRequest request
    ) {
        RoleResponse response = roleManagementService.createRole(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasPermission(null, 'ROLE_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<RoleResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        return ResponseEntity.ok(roleManagementService.updateRole(id, request));
    }

    @PreAuthorize("hasPermission(null, 'ROLE_ASSIGN_PERMISSION')")
    @PatchMapping("/{id}/permissions")
    public ResponseEntity<RoleResponse> assignPermissions(
            @PathVariable Long id,
            @Valid @RequestBody AssignRolePermissionsRequest request
    ) {
        return ResponseEntity.ok(roleManagementService.assignPermissions(id, request));
    }

    @PreAuthorize("hasPermission(null, 'ROLE_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleManagementService.deleteRole(id);

        return ResponseEntity.noContent().build();
    }
}