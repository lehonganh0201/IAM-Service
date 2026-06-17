package com.example.iamservice.service.impl;

import com.example.iamservice.config.properties.AppProperties;
import com.example.iamservice.config.properties.IdentityProviderType;
import com.example.iamservice.constant.EmailTemplate;
import com.example.iamservice.domain.dto.request.*;
import com.example.iamservice.domain.dto.response.AuthResponse;
import com.example.iamservice.domain.dto.response.IssuedRefreshToken;
import com.example.iamservice.domain.entity.RefreshToken;
import com.example.iamservice.domain.entity.User;
import com.example.iamservice.exception.NotFoundException;
import com.example.iamservice.exception.UnauthorizedException;
import com.example.iamservice.repository.UserRepository;
import com.example.iamservice.security.jwt.JwtTokenProvider;
import com.example.iamservice.service.AuthService;
import com.example.iamservice.service.EmailService;
import com.example.iamservice.service.RefreshTokenService;
import com.example.iamservice.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Map;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 14:40
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final int RESET_TOKEN_EXPIRY_MINUTES = 15;
    private static final String RESET_PASSWORD_PREFIX = "RESET_PASSWORD_TOKEN:";
    private final AppProperties appProperties;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.domain-url}")
    private String domainUrl;

    @Override
    @Transactional
    public AuthResponse login(AuthRequest request) {
        if (appProperties.getIdentityProvider().getType() != IdentityProviderType.SELF) {
            throw new IllegalStateException("Password login is disabled in Keycloak mode. Use Keycloak login URL.");
        }

        User user = findLoginUser(request.getUsernameOrEmail());

        validateUserCanLogin(user);

        String passwordHash = user.getPasswordHash();

        if (passwordHash == null || !passwordEncoder.matches(request.getPassword(), passwordHash)) {
            throw new BadCredentialsException("Invalid username/email or password");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        IssuedRefreshToken issuedRefreshToken = refreshTokenService.issue(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(issuedRefreshToken.rawToken())
                .tokenType("Bearer")
                .refreshTokenExpiresAt(issuedRefreshToken.expiresAt())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (appProperties.getIdentityProvider().getType() != IdentityProviderType.SELF) {
            throw new IllegalStateException("Self refresh is disabled in Keycloak mode. Use Keycloak refresh endpoint.");
        }

        RefreshToken refreshToken = refreshTokenService.validateActiveToken(request.getRefreshToken());

        User user = userRepository.findById(refreshToken.getUserId())
                .filter(u -> !Boolean.TRUE.equals(u.getDeleted()))
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        validateUserCanLogin(user);

        refreshToken.setRevoked(true);

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        IssuedRefreshToken newRefreshToken = refreshTokenService.issue(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken.rawToken())
                .tokenType("Bearer")
                .refreshTokenExpiresAt(newRefreshToken.expiresAt())
                .build();
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        if (appProperties.getIdentityProvider().getType() != IdentityProviderType.SELF) {
            throw new IllegalStateException("Self logout is disabled in Keycloak mode. Use Keycloak logout endpoint.");
        }

        refreshTokenService.revoke(request.getRefreshToken());
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());

        userRepository.findByEmailAndDeletedFalse(email).ifPresentOrElse(
                user -> processPasswordResetRequest(user, email),
                () -> logNonExistentEmailAttempt(email)
        );
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String token = request.getToken().trim();
        String redisKey = RESET_PASSWORD_PREFIX + token;

        String userIdStr = redisTemplate.opsForValue().getAndDelete(redisKey);
        if (userIdStr == null) {
            log.warn("Invalid or expired reset token used: {}", token);
            throw new UnauthorizedException("Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn");
        }

        User user = findUserWithId(Long.parseLong(userIdStr));
        updateUserPassword(user, request.getNewPassword());
        sendPasswordChangedNotification(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private void processPasswordResetRequest(User user, String email) {
        String resetToken = generateResetToken();
        String redisKey = RESET_PASSWORD_PREFIX + resetToken;

        redisTemplate.opsForValue().set(redisKey, user.getId().toString(),
                Duration.ofMinutes(RESET_TOKEN_EXPIRY_MINUTES));

        String resetLink = buildResetLink(resetToken);

        log.info("Generated password reset link for user: {}", email);

        sendPasswordResetEmail(user, resetLink);
    }

    private String generateResetToken() {
        return RandomUtil.randomResetToken();
    }

    private String buildResetLink(String resetToken) {
        return domainUrl + "/reset-password?token=" + resetToken;
    }

    private void sendPasswordResetEmail(User user, String resetLink) {
        try {
            emailService.sendEmail(
                    user.getEmail(),
                    EmailTemplate.PASSWORD_RESET,
                    Map.of(
                            "userName", user.getDisplayName(),
                            "resetLink", resetLink,
                            "expiryMinutes", RESET_TOKEN_EXPIRY_MINUTES
                    )
            );
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
        }
    }

    private User findLoginUser(String usernameOrEmail) {
        return userRepository.findByUsernameAndDeletedFalse(usernameOrEmail)
                .or(() -> userRepository.findByEmailAndDeletedFalse(usernameOrEmail))
                .orElseThrow(() -> new BadCredentialsException("Invalid username/email or password"));
    }

    private void validateUserCanLogin(User user) {
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new DisabledException("User is disabled");
        }

        if (Boolean.TRUE.equals(user.getLocked())) {
            throw new LockedException("User is locked");
        }

        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new DisabledException("User is deleted");
        }
    }

    private void logNonExistentEmailAttempt(String email) {
        log.warn("Forgot password attempt for non-existent email: {}", email);
    }

    private void updateUserPassword(User user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password reset successfully for user ID: {}", user.getId());
    }

    private void sendPasswordChangedNotification(User user) {
        try {
            emailService.sendEmail(
                    user.getEmail(),
                    EmailTemplate.PASSWORD_CHANGED,
                    Map.of(
                            "userName", user.getDisplayName(),
                            "email", user.getEmail()
                    )
            );
        } catch (Exception e) {
            log.warn("Failed to send password changed notification to: {}", user.getEmail(), e);
        }
    }

    private User findUserWithId(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
