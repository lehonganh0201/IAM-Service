package com.example.iamservice.service;

import com.example.iamservice.domain.dto.request.UserRequest;
import com.example.iamservice.domain.dto.response.UserResponse;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 15:32
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */


public interface UserService {
    UserResponse register(UserRequest request);

    UserResponse getMe(String token);
}
