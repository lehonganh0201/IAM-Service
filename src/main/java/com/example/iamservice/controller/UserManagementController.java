package com.example.iamservice.controller;

import com.example.iamservice.base.PageResponse;
import com.example.iamservice.domain.dto.request.AssignUserRolesRequest;
import com.example.iamservice.domain.dto.request.CreateUserRequest;
import com.example.iamservice.domain.dto.request.ResetUserPasswordRequest;
import com.example.iamservice.domain.dto.request.UpdateUserRequest;
import com.example.iamservice.domain.dto.response.UserResponse;
import com.example.iamservice.service.UserManagementService;
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
 * Created on:    17/06/2026 at 15:25
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;

    @PreAuthorize("hasPermission(null, 'USER_READ')")
    @GetMapping("/users")
    public ResponseEntity<PageResponse<UserResponse>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(userManagementService.getUsers(keyword, pageable));
    }

    @PreAuthorize("hasPermission(null, 'USER_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.getUserById(id));
    }

    @PreAuthorize("hasPermission(null, 'USER_CREATE')")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserResponse response = userManagementService.createUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasPermission(null, 'USER_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userManagementService.updateUser(id, request));
    }

    @PreAuthorize("hasPermission(null, 'USER_LOCK')")
    @PatchMapping("/{id}/lock")
    public ResponseEntity<UserResponse> lockUser(@PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.lockUser(id));
    }

    @PreAuthorize("hasPermission(null, 'USER_UNLOCK')")
    @PatchMapping("/{id}/unlock")
    public ResponseEntity<UserResponse> unlockUser(@PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.unlockUser(id));
    }

    @PreAuthorize("hasPermission(null, 'USER_ASSIGN_ROLE')")
    @PatchMapping("/{id}/roles")
    public ResponseEntity<UserResponse> assignRoles(
            @PathVariable Long id,
            @Valid @RequestBody AssignUserRolesRequest request
    ) {
        return ResponseEntity.ok(userManagementService.assignRoles(id, request));
    }

    @PreAuthorize("hasPermission(null, 'USER_RESET_PASSWORD')")
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetUserPasswordRequest request
    ) {
        userManagementService.resetPassword(id, request);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasPermission(null, 'USER_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userManagementService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}