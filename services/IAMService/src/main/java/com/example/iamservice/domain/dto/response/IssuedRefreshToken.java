package com.example.iamservice.domain.dto.response;

import java.time.Instant;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 11:32
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */


public record IssuedRefreshToken(
        String rawToken,
        Instant expiresAt
) {
}
