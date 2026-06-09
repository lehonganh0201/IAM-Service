package com.example.iamservice.service;

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

    AuthResponse login(UserRequest request);

    AuthResponse refreshToken(String token);
}
