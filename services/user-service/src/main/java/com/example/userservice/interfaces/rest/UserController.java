package com.example.userservice.interfaces.rest;

import com.example.commonlib.api.RestApiV1;
import com.example.commonlib.api.common.ApiResponse;
import com.example.commonlib.api.common.ApiResponseFactory;
import com.example.commonlib.api.common.PageResponse;
import com.example.userservice.application.dto.request.UpdateUserRequest;
import com.example.userservice.application.dto.request.UserSearchQuery;
import com.example.userservice.application.dto.request.CreateUserRequest;
import com.example.userservice.application.dto.response.UserResponse;
import com.example.userservice.application.usecase.AvatarUseCase;
import com.example.userservice.application.usecase.UserUseCases;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 15:12
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@RestApiV1
@RequiredArgsConstructor
public class UserController {
    private final UserUseCases useCases;
    private final AvatarUseCase avatarUseCase;
    private final ApiResponseFactory responseFactory;

    @PostMapping("/users")
    @PreAuthorize("hasAnyAuthority('iam:user:manage','ROLE_admin')")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest r) {
        return ResponseEntity.ok(
                responseFactory.success("Created successfully", useCases.create(r)));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(
                responseFactory.success(
                        "Get user successfully",
                        useCases.get(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> list(@RequestParam(required = false) String keyword,
                                                                        @RequestParam(required = false) String province,
                                                                        @RequestParam(required = false) Double minYears,
                                                                        @RequestParam(required = false) Double maxYears,
                                                                        Pageable pageable) {
        return ResponseEntity.ok(
                responseFactory.success(
                        "Get user list successfully",
                        useCases.search(new UserSearchQuery(keyword, province, minYears, maxYears), pageable)
                )
        );
    }

    @PatchMapping("/users/{id}")
    @PreAuthorize("hasAnyAuthority('iam:user:manage','ROLE_admin')")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest r) {
        return ResponseEntity.ok(
                responseFactory.success(
                        "Updated successfully",
                        useCases.update(id, r)
                )
        );
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAnyAuthority('iam:user:manage','ROLE_admin')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        useCases.delete(id);
        return ResponseEntity.ok(
                responseFactory.success(
                        "Deleted successfully", null)
        );
    }

    @PostMapping(value = "/users/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('iam:user:manage','ROLE_admin')")
    public ResponseEntity<ApiResponse<UserResponse>> uploadAvatar(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
                responseFactory.success(
                        "Avatar uploaded", avatarUseCase.uploadAvatar(id, file)
                )
        );
    }

    @DeleteMapping("/{id}/avatar")
    @PreAuthorize("hasAnyAuthority('iam:user:manage','ROLE_admin')")
    public ResponseEntity<ApiResponse<UserResponse>> deleteAvatar(@PathVariable UUID id) {
        return ResponseEntity.ok(
                responseFactory.success(
                        "Avatar deleted",
                        avatarUseCase.deleteAvatar(id))
        );
    }
}
