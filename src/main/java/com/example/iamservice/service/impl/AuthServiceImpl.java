package com.example.iamservice.service.impl;

import com.example.iamservice.config.properties.AppProperties;
import com.example.iamservice.config.properties.IdentityProviderType;
import com.example.iamservice.constant.AuditAction;
import com.example.iamservice.constant.AuditResourceType;
import com.example.iamservice.constant.AuditResult;
import com.example.iamservice.constant.EmailTemplate;
import com.example.iamservice.domain.dto.request.*;
import com.example.iamservice.domain.dto.response.AuthResponse;
import com.example.iamservice.domain.dto.response.IssuedRefreshToken;
import com.example.iamservice.domain.dto.response.KeycloakLoginResponse;
import com.example.iamservice.domain.dto.response.KeycloakTokenResponse;
import com.example.iamservice.domain.entity.RefreshToken;
import com.example.iamservice.domain.entity.User;
import com.example.iamservice.exception.BadRequestException;
import com.example.iamservice.exception.NotFoundException;
import com.example.iamservice.exception.UnauthorizedException;
import com.example.iamservice.repository.UserRepository;
import com.example.iamservice.security.jwt.JwtTokenProvider;
import com.example.iamservice.service.AuthService;
import com.example.iamservice.service.EmailService;
import com.example.iamservice.service.RefreshTokenService;
import com.example.iamservice.util.AuditRequestInfoProvider;
import com.example.iamservice.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
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
    private final KeycloakUserService keycloakUserService;
    private final AuditLogService auditLogService;
    private final AuditRequestInfoProvider auditRequestInfoProvider;

    @Value("${app.domain-url}")
    private String domainUrl;

    @Override
    @Transactional
    public AuthResponse login(AuthRequest request) {
        if (isKeycloakMode()) {
            throw new BadRequestException("Password login is disabled in Keycloak mode. Use Keycloak login URL.");
        }

        User user = findLoginUser(request.getUsernameOrEmail());

        validateUserCanLogin(user);

        String passwordHash = user.getPasswordHash();

        if (passwordHash == null || !passwordEncoder.matches(request.getPassword(), passwordHash)) {
            auditLogService.saveAuthAudit(
                    AuditAction.AUTH_LOGIN_FAILURE,
                    AuditResult.FAILURE,
                    request.getUsernameOrEmail(),
                    null,
                    "Invalid username/email or password"
            );

            throw new UnauthorizedException("Invalid username/email or password");
        }

        IssuedRefreshToken issuedRefreshToken = refreshTokenService.issue(user.getId());
        String accessToken = jwtTokenProvider.generateAccessToken(user);

        auditLogService.saveAuthAudit(
                AuditAction.AUTH_LOGIN_SUCCESS,
                AuditResult.SUCCESS,
                user.getUsername(),
                user.getId(),
                null
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(issuedRefreshToken.rawToken())
                .tokenType("Bearer")
                .refreshTokenExpiresAt(issuedRefreshToken.expiresAt())
                .build();
    }

    @Override
    public KeycloakLoginResponse getLoginUrl(String provider) {
        if (!isKeycloakMode()) {
            throw new BadRequestException("Keycloak login URL is only available in Keycloak mode");
        }

        return new KeycloakLoginResponse(
                "Please login via Keycloak",
                keycloakUserService.buildAuthorizationUrl(provider),
                provider
        );
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (isKeycloakMode()) {
            KeycloakTokenResponse tokenResponse = keycloakUserService.refresh(request.getRefreshToken());

            return AuthResponse.builder()
                    .accessToken(tokenResponse.accessToken())
                    .refreshToken(tokenResponse.refreshToken())
                    .tokenType(tokenResponse.tokenType())
                    .refreshTokenExpiresAt(
                            Instant.now().plusSeconds(tokenResponse.refreshExpiresIn())
                    )
                    .build();
        }

        return refreshSelfToken(request);
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        if (isKeycloakMode()) {
            keycloakUserService.logout(request.getRefreshToken());

            logoutAuthLog();
            return;
        }

        logoutAuthLog();
        refreshTokenService.revoke(request.getRefreshToken());
    }

    private boolean isKeycloakMode() {
        return appProperties.getIdentityProvider().getType() == IdentityProviderType.KEYCLOAK;
    }

    @Override
    @Transactional
    public AuthResponse exchangeKeycloakCode(
            KeycloakCodeExchangeRequest request
    ) {
        if (!isKeycloakMode()) {
            throw new BadRequestException("Keycloak mode is not enabled");
        }

        KeycloakTokenResponse tokenResponse = keycloakUserService.exchangeCode(
                request.getCode(),
                request.getRedirectUri(),
                request.getCodeVerifier()
        );

        return AuthResponse.builder()
                .accessToken(tokenResponse.accessToken())
                .refreshToken(tokenResponse.refreshToken())
                .tokenType(tokenResponse.tokenType())
                .refreshTokenExpiresAt(Instant.now().plusSeconds(tokenResponse.refreshExpiresIn()))
                .build();
    }

    private void logoutAuthLog() {
        auditLogService.save(
                AuditLogCommand.builder()
                        .action(AuditAction.AUTH_LOGOUT)
                        .resourceType(AuditResourceType.AUTH)
                        .result(AuditResult.SUCCESS)
                        .message("Logout")
                        .httpMethod(auditRequestInfoProvider.method())
                        .requestPath(auditRequestInfoProvider.path())
                        .ipAddress(auditRequestInfoProvider.ipAddress())
                        .userAgent(auditRequestInfoProvider.userAgent())
                        .requestId(auditRequestInfoProvider.requestId())
                        .build()
        );
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
                .orElseThrow(() -> new UnauthorizedException("Invalid username/email or password"));
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

    private AuthResponse refreshSelfToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.validateActiveToken(request.getRefreshToken());

        User user = userRepository.findById(refreshToken.getUserId())
                .filter(u -> !Boolean.TRUE.equals(u.getDeleted()))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        validateUserCanLogin(user);

        refreshToken.setRevoked(true);

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        IssuedRefreshToken newRefreshToken = refreshTokenService.issue(user.getId());

        auditLogService.save(
                AuditLogCommand.builder()
                        .actorUserId(user.getId())
                        .actorUsername(user.getUsername())
                        .actorEmail(user.getEmail())
                        .identityProvider("SELF")
                        .action(AuditAction.AUTH_REFRESH_TOKEN)
                        .resourceType(AuditResourceType.AUTH)
                        .result(AuditResult.SUCCESS)
                        .message("Refresh token")
                        .httpMethod(auditRequestInfoProvider.method())
                        .requestPath(auditRequestInfoProvider.path())
                        .ipAddress(auditRequestInfoProvider.ipAddress())
                        .userAgent(auditRequestInfoProvider.userAgent())
                        .requestId(auditRequestInfoProvider.requestId())
                        .build()
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken.rawToken())
                .tokenType("Bearer")
                .refreshTokenExpiresAt(newRefreshToken.expiresAt())
                .build();
    }
}
