package com.example.iamservice.service;

import com.example.iamservice.domain.dto.request.AuthRequest;
import com.example.iamservice.domain.dto.request.ForgotPasswordRequest;
import com.example.iamservice.domain.dto.request.ResetPasswordRequest;
import com.example.iamservice.domain.dto.request.UserRequest;
import com.example.iamservice.domain.dto.response.AuthResponse;

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

    AuthResponse refreshToken(String token);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void logout(String token);
}
