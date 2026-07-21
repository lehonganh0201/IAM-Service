package com.example.iamservice.service.impl;

import com.example.commonlib.exception.BadRequestException;
import com.example.commonlib.exception.NotFoundException;
import com.example.iamservice.config.properties.AppProperties;
import com.example.iamservice.domain.dto.response.IssuedRefreshToken;
import com.example.iamservice.domain.entity.RefreshToken;
import com.example.iamservice.repository.RefreshTokenRepository;
import com.example.iamservice.util.TokenHashingService;
import com.example.iamservice.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 11:33
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Service
@Log4j2
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private static final int REFRESH_TOKEN_BYTES = 64;

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHashingService tokenHashingService;
    private final AppProperties appProperties;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public IssuedRefreshToken issue(Long userId) {
        String rawToken = generateSecureToken();
        String tokenHash = tokenHashingService.sha256(rawToken);

        Instant expiresAt = Instant.now()
                .plusMillis(appProperties.getJwt().getRefreshExpirationMs());

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return new IssuedRefreshToken(rawToken, expiresAt);
    }

    @Override
    @Transactional
    public RefreshToken validateActiveToken(String rawToken) {
        String tokenHash = tokenHashingService.sha256(rawToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .orElseThrow(() -> new NotFoundException("Invalid refresh token"));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new BadRequestException("Refresh token expired");
        }

        return refreshToken;
    }

    @Override
    @Transactional
    public void revoke(String rawToken) {
        String tokenHash = tokenHashingService.sha256(rawToken);

        refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                });
    }

    @Transactional
    public void revokeAllByUserId(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
