package com.example.iamservice.controller;

import com.example.iamservice.base.RestApiV1;
import com.example.iamservice.base.RestData;
import com.example.iamservice.base.VsResponseUtil;
import com.example.iamservice.domain.dto.request.AuthRequest;
import com.example.iamservice.domain.dto.request.ForgotPasswordRequest;
import com.example.iamservice.domain.dto.request.ResetPasswordRequest;
import com.example.iamservice.domain.dto.request.VerifyEmailRequest;
import com.example.iamservice.domain.dto.response.AuthResponse;
import com.example.iamservice.service.AuthService;
import com.example.iamservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.http.HttpStatus.*;

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
    private final UserService userService;

    @PostMapping("/auth/login")
    public ResponseEntity<RestData<AuthResponse>> login(@RequestBody @Valid AuthRequest request) {
        return VsResponseUtil.success(authService.login(request), "Login success", OK);
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<RestData<AuthResponse>> refreshToken(@RequestHeader(name = HttpHeaders.AUTHORIZATION) String token) {
        return VsResponseUtil.success(authService.refreshToken(token), "Refresh token success", OK);
    }

    @PostMapping("/auth/forgot-password")
    public ResponseEntity<RestData<Void>> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return VsResponseUtil.success(null, "Your password will be send in your email if exists", OK);
    }

    @PostMapping("/auth/reset-password")
    public ResponseEntity<RestData<Void>> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authService.resetPassword(request);
        return VsResponseUtil.success(null, "Your password change success", OK);
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<RestData<Void>> logout(@RequestHeader(name = HttpHeaders.AUTHORIZATION) String token) {
        authService.logout(token);
        return VsResponseUtil.success(null, "Logout success", NO_CONTENT);
    }

    @PostMapping("/auth/verify-email")
    public ResponseEntity<RestData<Void>> verifyEmail(@RequestBody VerifyEmailRequest request) {
        userService.verifyEmail(request);
        return VsResponseUtil.success(null, "Email was verified success", OK);
    }

    @PostMapping("/auth/resend-verification")
    public ResponseEntity<RestData<Void>> resendVerification(@RequestParam String email) {
        userService.resendVerificationEmail(email);
        return VsResponseUtil.success(null, "Email verify was resend success", OK);
    }
}
