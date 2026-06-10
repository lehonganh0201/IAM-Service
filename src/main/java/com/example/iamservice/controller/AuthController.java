package com.example.iamservice.controller;

import com.example.iamservice.base.RestApiV1;
import com.example.iamservice.base.RestData;
import com.example.iamservice.base.VsResponseUtil;
import com.example.iamservice.domain.dto.request.AuthRequest;
import com.example.iamservice.domain.dto.request.UserRequest;
import com.example.iamservice.domain.dto.response.AuthResponse;
import com.example.iamservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 14:32
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@RestApiV1
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/auth/login")
    public ResponseEntity<RestData<AuthResponse>> login(@RequestBody @Valid AuthRequest request) {
        return VsResponseUtil.success(authService.login(request), "Login success", OK);
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<RestData<AuthResponse>> refreshToken(@RequestHeader(name = HttpHeaders.AUTHORIZATION) String token) {
        return VsResponseUtil.success(authService.refreshToken(token), "Refresh token success", OK);
    }
}
