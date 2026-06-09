package com.example.iamservice.controller;

import com.example.iamservice.base.RestApiV1;
import com.example.iamservice.base.RestData;
import com.example.iamservice.base.VsResponseUtil;
import com.example.iamservice.domain.dto.request.UserRequest;
import com.example.iamservice.domain.dto.response.AuthResponse;
import com.example.iamservice.domain.dto.response.UserResponse;
import com.example.iamservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

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
}
