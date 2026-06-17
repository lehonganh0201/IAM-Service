package com.example.iamservice.controller;

import com.example.iamservice.domain.dto.request.*;
import com.example.iamservice.domain.dto.response.UserResponse;
import com.example.iamservice.domain.dto.response.common.ApiResponse;
import com.example.iamservice.domain.dto.response.common.ApiResponseFactory;
import com.example.iamservice.domain.dto.response.common.PageResponse;
import com.example.iamservice.domain.dto.response.common.PageableFactory;
import com.example.iamservice.service.UserManagementService;
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
 * Created on:    17/06/2026 at 15:25
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private static final Set<String> USER_SORT_FIELDS = Set.of(
            "id",
            "username",
            "email",
            "firstName",
            "lastName",
            "createdAt",
            "updatedAt"
    );

    private final UserManagementService userManagementService;
    private final ApiResponseFactory responseFactory;
    private final PageableFactory pageableFactory;

    @PreAuthorize("hasPermission(null, 'USER_READ')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsers(
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
                USER_SORT_FIELDS
        );

        PageResponse<UserResponse> data =
                userManagementService.getUsers(keyword, pageable);

        return ResponseEntity.ok(
                responseFactory.success("Get users successfully", data)
        );
    }

    @PreAuthorize("hasPermission(null, 'USER_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse data = userManagementService.getUserById(id);

        return ResponseEntity.ok(
                responseFactory.success("Get user successfully", data)
        );
    }

    @PreAuthorize("hasPermission(null, 'USER_CREATE')")
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserResponse data = userManagementService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseFactory.success("Create user successfully", data));
    }

    @PreAuthorize("hasPermission(null, 'USER_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        UserResponse data = userManagementService.updateUser(id, request);

        return ResponseEntity.ok(
                responseFactory.success("Update user successfully", data)
        );
    }

    @PreAuthorize("hasPermission(null, 'USER_LOCK')")
    @PatchMapping("/{id}/lock")
    public ResponseEntity<ApiResponse<UserResponse>> lockUser(@PathVariable Long id) {
        UserResponse data = userManagementService.lockUser(id);

        return ResponseEntity.ok(
                responseFactory.success("Lock user successfully", data)
        );
    }

    @PreAuthorize("hasPermission(null, 'USER_UNLOCK')")
    @PatchMapping("/{id}/unlock")
    public ResponseEntity<ApiResponse<UserResponse>> unlockUser(@PathVariable Long id) {
        UserResponse data = userManagementService.unlockUser(id);

        return ResponseEntity.ok(
                responseFactory.success("Unlock user successfully", data)
        );
    }

    @PreAuthorize("hasPermission(null, 'USER_ASSIGN_ROLE')")
    @PatchMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<UserResponse>> assignRoles(
            @PathVariable Long id,
            @Valid @RequestBody AssignUserRolesRequest request
    ) {
        UserResponse data = userManagementService.assignRoles(id, request);

        return ResponseEntity.ok(
                responseFactory.success("Assign user roles successfully", data)
        );
    }

    @PreAuthorize("hasPermission(null, 'USER_RESET_PASSWORD')")
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetUserPasswordRequest request
    ) {
        userManagementService.resetPassword(id, request);

        return ResponseEntity.ok(
                responseFactory.success("Reset user password successfully")
        );
    }

    @PreAuthorize("hasPermission(null, 'USER_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            @RequestBody(required = false) DeleteReasonRequest request
    ) {
        String reason = request == null ? null : request.getReason();

        userManagementService.deleteUser(id, reason);

        return ResponseEntity.ok(
                responseFactory.success("Delete user successfully")
        );
    }
}