package com.example.iamservice.service;

import com.example.iamservice.domain.dto.request.*;
import com.example.iamservice.domain.dto.response.AuthResponse;
import com.example.iamservice.domain.dto.response.KeycloakLoginResponse;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 14:36
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */


public interface AuthService {

    AuthResponse login(AuthRequest request);

    KeycloakLoginResponse getLoginUrl();

    AuthResponse refreshToken(RefreshTokenRequest token);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void logout(LogoutRequest token);
}
