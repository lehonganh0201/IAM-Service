package com.example.iamservice.controller;

import com.example.iamservice.aop.annotation.RateLimit;
import com.example.iamservice.base.RestApiV1;
import com.example.iamservice.base.RestData;
import com.example.iamservice.base.VsResponseUtil;
import com.example.iamservice.domain.dto.request.*;
import com.example.iamservice.domain.dto.response.AuthResponse;
import com.example.iamservice.domain.dto.response.KeycloakLoginResponse;
import com.example.iamservice.service.AuthService;
import com.example.iamservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @RateLimit(
            key = "login",
            capacity = 5,
            refillAmount = 5,
            refillDurationMinutes = 1,
            strategy = "COMBINED"
    )
    @PostMapping("/auth/login")
    public ResponseEntity<RestData<AuthResponse>> login(@RequestBody @Valid AuthRequest request) {
        return VsResponseUtil.success(authService.login(request), "Login success", OK);
    }

    @GetMapping("/auth/login")
    public ResponseEntity<RestData<KeycloakLoginResponse>> getLoginUrl() {
        return VsResponseUtil.success(authService.getLoginUrl(), "Login success", OK);
    }

    @RateLimit(
            key = "refresh-token",
            capacity = 10,
            refillAmount = 10,
            refillDurationMinutes = 10,
            strategy = "USER"
    )
    @PostMapping("/auth/refresh")
    public ResponseEntity<RestData<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return VsResponseUtil.success(authService.refreshToken(request), "Refresh token success", OK);
    }

    @RateLimit(
            key = "forgot-password",
            capacity = 3,
            refillAmount = 3,
            refillDurationMinutes = 60,
            strategy = "EMAIL"
    )
    @PostMapping("/auth/forgot-password")
    public ResponseEntity<RestData<Void>> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return VsResponseUtil.success(null, "Your password will be send in your email if exists", OK);
    }

    @RateLimit(
            key = "reset-password",
            capacity = 5,
            refillAmount = 5,
            refillDurationMinutes = 15,
            strategy = "IP"
    )
    @PostMapping("/auth/reset-password")
    public ResponseEntity<RestData<Void>> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authService.resetPassword(request);
        return VsResponseUtil.success(null, "Your password change success", OK);
    }

    @RateLimit(
            key = "logout",
            capacity = 20,
            refillAmount = 20,
            refillDurationMinutes = 10,
            strategy = "USER"
    )
    @PostMapping("/auth/logout")
    public ResponseEntity<RestData<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return VsResponseUtil.success(null, "Logout success", NO_CONTENT);
    }

    @RateLimit(
            key = "verify-email",
            capacity = 5,
            refillAmount = 5,
            refillDurationMinutes = 10,
            strategy = "IP"
    )
    @PostMapping("/auth/verify-email")
    public ResponseEntity<RestData<Void>> verifyEmail(@RequestBody VerifyEmailRequest request) {
        userService.verifyEmail(request);
        return VsResponseUtil.success(null, "Email was verified success", OK);
    }

    @RateLimit(
            key = "resend-verification",
            capacity = 3,
            refillAmount = 3,
            refillDurationMinutes = 30,
            strategy = "EMAIL"
    )
    @PostMapping("/auth/resend-verification")
    public ResponseEntity<RestData<Void>> resendVerification(@RequestParam String email) {
        userService.resendVerificationEmail(email);
        return VsResponseUtil.success(null, "Email verify was resend success", OK);
    }
}
