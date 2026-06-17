package com.example.iamservice.service;

import com.example.iamservice.domain.dto.response.IssuedRefreshToken;
import com.example.iamservice.domain.entity.RefreshToken;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 11:30
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */


public interface RefreshTokenService {
    IssuedRefreshToken issue(Long userId);
    RefreshToken validateActiveToken(String rawToken);
    void revoke(String rawToken);
}
