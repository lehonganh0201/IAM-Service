package com.example.iamservice.controller;

import com.example.commonlib.api.common.ApiResponse;
import com.example.commonlib.api.common.ApiResponseFactory;
import com.example.commonlib.api.common.PageResponse;
import com.example.commonlib.api.common.PageableFactory;
import com.example.iamservice.aop.annotation.AuditActivity;
import com.example.iamservice.constant.AuditAction;
import com.example.iamservice.constant.AuditResourceType;
import com.example.iamservice.domain.dto.request.*;
import com.example.iamservice.domain.dto.response.ImportResultResponse;
import com.example.iamservice.domain.dto.response.UserResponse;
import com.example.iamservice.service.UserManagementService;
import com.example.iamservice.service.exporter.ExportService;
import com.example.iamservice.service.importer.ImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final ImportService importService;
    private final ExportService exportService;
    private final ApiResponseFactory responseFactory;
    private final PageableFactory pageableFactory;

    @PreAuthorize("hasAnyAuthority('iam:user:manage','ROLE_admin')")
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

    @PreAuthorize("hasAnyAuthority('iam:user:manage','ROLE_admin')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse data = userManagementService.getUserById(id);

        return ResponseEntity.ok(
                responseFactory.success("Get user successfully", data)
        );
    }

    @AuditActivity(
            action = AuditAction.USER_CREATE,
            resourceType = AuditResourceType.USER,
            message = "Create user"
    )
    @PreAuthorize("hasAnyAuthority('iam:user:manage','ROLE_admin')")
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserResponse data = userManagementService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseFactory.success("Create user successfully", data));
    }

    @AuditActivity(
            action = AuditAction.USER_UPDATE,
            resourceType = AuditResourceType.USER,
            resourceIdParam = "id",
            message = "Update user"
    )
    @PreAuthorize("hasAnyAuthority('iam:user:manage','ROLE_admin')")
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

    @AuditActivity(
            action = AuditAction.USER_LOCK,
            resourceType = AuditResourceType.USER,
            resourceIdParam = "id",
            message = "Lock user"
    )
    @PreAuthorize("hasAnyAuthority('iam:user:manage','ROLE_admin')")
    @PatchMapping("/{id}/lock")
    public ResponseEntity<ApiResponse<UserResponse>> lockUser(@PathVariable Long id) {
        UserResponse data = userManagementService.lockUser(id);

        return ResponseEntity.ok(
                responseFactory.success("Lock user successfully", data)
        );
    }

    @AuditActivity(
            action = AuditAction.USER_UNLOCK,
            resourceType = AuditResourceType.USER,
            resourceIdParam = "id",
            message = "Unlock user"
    )
    @PreAuthorize("hasAnyAuthority('iam:user:manage','ROLE_admin')")
    @PatchMapping("/{id}/unlock")
    public ResponseEntity<ApiResponse<UserResponse>> unlockUser(@PathVariable Long id) {
        UserResponse data = userManagementService.unlockUser(id);

        return ResponseEntity.ok(
                responseFactory.success("Unlock user successfully", data)
        );
    }

    @AuditActivity(
            action = AuditAction.USER_ASSIGN_ROLE,
            resourceType = AuditResourceType.USER,
            resourceIdParam = "id",
            message = "Assign roles to user"
    )
    @PreAuthorize("hasAnyAuthority('iam:user:manage','ROLE_admin')")
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

    @AuditActivity(
            action = AuditAction.USER_RESET_PASSWORD,
            resourceType = AuditResourceType.USER,
            resourceIdParam = "id",
            message = "Reset user password"
    )
    @PreAuthorize("hasAnyAuthority('iam:user:manage','ROLE_admin')")
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

    @AuditActivity(
            action = AuditAction.USER_DELETE,
            resourceType = AuditResourceType.USER,
            resourceIdParam = "id",
            message = "Delete user"
    )
    @PreAuthorize("hasAnyAuthority('iam:user:manage','ROLE_admin')")
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

    @GetMapping("/import/template")
    @PreAuthorize("hasAnyAuthority('iam:user:import','ROLE_admin')")
    public ResponseEntity<Resource> template() {
        byte[] b = importService.template();
        return ResponseEntity.ok().contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("user-import-template.xlsx")
                                .build().toString()).contentLength(b.length).body(new ByteArrayResource(b));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('iam:user:import','ROLE_admin')")
    public ResponseEntity<ApiResponse<ImportResultResponse>> importUsers(@RequestPart MultipartFile file,
                                                                         @RequestParam(defaultValue = "true") boolean dryRun) {
        return ResponseEntity.ok(
                responseFactory.success(
                        "Import processed",
                        importService.importUsers(file, dryRun))
        );
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyAuthority('iam:user:export','ROLE_admin')")
    public ResponseEntity<Resource> export(@RequestParam(defaultValue = "xlsx") String format, @RequestParam(required = false) String keyword, @RequestParam(required = false) String province, @RequestParam(required = false) Double minYears, @RequestParam(required = false) Double maxYears) {
        var f = exportService.export(format, new UserSearchQuery(keyword, province, minYears, maxYears));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(f.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(f.filename()).build().toString()).contentLength(f.bytes().length)
                .body(new ByteArrayResource(f.bytes()));
    }
}