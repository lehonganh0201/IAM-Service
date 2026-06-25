package com.example.iamservice.domain.dto.response;

import lombok.*;

import java.time.Instant;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 14:36
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Builder
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Instant refreshTokenExpiresAt
) {
}
