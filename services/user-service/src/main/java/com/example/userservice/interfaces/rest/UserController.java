package com.example.userservice.interfaces.rest;

import com.example.commonlib.api.RestApiV1;
import com.example.commonlib.api.common.ApiResponse;
import com.example.commonlib.api.common.ApiResponseFactory;
import com.example.userservice.application.dto.request.CreateUserRequest;
import com.example.userservice.application.dto.response.UserResponse;
import com.example.userservice.application.usecase.UserUseCases;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
    private final ApiResponseFactory responseFactory;

    @PostMapping("/users")
    @PreAuthorize("hasAnyAuthority('iam:user:manage','ROLE_admin')")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest r) {
        return ResponseEntity.ok(responseFactory.success("Created successfully", useCases.create(r)));
    }
}
