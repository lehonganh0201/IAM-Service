package com.example.iamservice.controller;

import com.example.iamservice.base.RestApiV1;
import com.example.iamservice.base.RestData;
import com.example.iamservice.base.VsResponseUtil;
import com.example.iamservice.domain.dto.request.UpdateUserPasswordRequest;
import com.example.iamservice.domain.dto.request.UpdateUserRequest;
import com.example.iamservice.domain.dto.request.UserRequest;
import com.example.iamservice.domain.dto.response.UserResponse;
import com.example.iamservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 15:38
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@RestApiV1
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/users")
    public ResponseEntity<RestData<UserResponse>> register(@RequestBody @Valid UserRequest request) {
        return VsResponseUtil.success(userService.register(request), "Register success", CREATED);
    }

    @GetMapping("/users")
    public ResponseEntity<RestData<UserResponse>> getMe(@RequestHeader(name = HttpHeaders.AUTHORIZATION) String token) {
        return VsResponseUtil.success(userService.getMe(token), "Get my info success", OK);
    }

    @PutMapping("/users")
    public ResponseEntity<RestData<UserResponse>> updateUser(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @ModelAttribute @Valid UpdateUserRequest request) {
        return VsResponseUtil.success(userService.updateUser(token, request), "Update user success", OK);
    }

    @PutMapping("/users/password")
    public ResponseEntity<RestData<UserResponse>> updateUserPassword(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestBody @Valid UpdateUserPasswordRequest request) {
        return VsResponseUtil.success(userService.updateUserPassword(token, request), "Update user password success", OK);
    }
}
