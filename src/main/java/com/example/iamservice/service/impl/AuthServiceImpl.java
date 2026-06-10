package com.example.iamservice.service.impl;

import com.example.iamservice.constant.EmailTemplate;
import com.example.iamservice.domain.dto.request.AuthRequest;
import com.example.iamservice.domain.dto.request.ForgotPasswordRequest;
import com.example.iamservice.domain.dto.request.ResetPasswordRequest;
import com.example.iamservice.domain.dto.response.AuthResponse;
import com.example.iamservice.domain.entity.User;
import com.example.iamservice.exception.BadRequestException;
import com.example.iamservice.exception.NotFoundException;
import com.example.iamservice.exception.UnauthorizedException;
import com.example.iamservice.repository.UserRepository;
import com.example.iamservice.security.UserPrincipal;
import com.example.iamservice.security.jwt.JwtTokenProvider;
import com.example.iamservice.service.AuthService;
import com.example.iamservice.service.EmailService;
import com.example.iamservice.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private final AuthenticationManager authenticationManager;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final int RESET_TOKEN_EXPIRY_MINUTES = 15;
    private static final boolean IS_REFRESH_TOKEN = true;
    private static final String RESET_PASSWORD_PREFIX = "RESET_PASSWORD_TOKEN:";
    private static final String BLACKLIST_TOKEN_PREFIX = "BLACKLIST_TOKEN:";

    @Value("${app.domain-url}")
    private String domainUrl;

    @Override
    public AuthResponse login(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            if (Objects.isNull(authentication)) {
                throw new UnauthorizedException("Login failed with wrong email or password");
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("User logged in successfully: {}", request.getEmail());
            return generateAuthResponse((UserPrincipal) authentication.getPrincipal());

        } catch (Exception ex) {
            log.warn("Login failed for username or email {}: {}", request.getEmail(), ex.getMessage());
            throw new UnauthorizedException("Login failed");
        }
    }

    @Override
    public AuthResponse refreshToken(String token) {
        String cleanedToken = cleanToken(token);

        if (jwtTokenProvider.validateToken(cleanedToken) && jwtTokenProvider.isRefreshToken(cleanedToken)) {
            String username = jwtTokenProvider.extractUsername(cleanedToken);
            User user = findUserWithEmail(username);

            log.info("Token refreshed for user: {}", username);

            return generateAuthResponse(UserPrincipal.create(user));
        }
        throw new UnauthorizedException("Refresh token cannot access");
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());

        userRepository.findByEmail(email).ifPresentOrElse(
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

    @Override
    public void logout(String token) {
        String cleanedToken = cleanToken(token);

        if (!jwtTokenProvider.validateToken(cleanedToken) || jwtTokenProvider.isRefreshToken(cleanedToken)) {
            throw new BadRequestException("Token không hợp lệ hoặc là refresh token");
        }

        long expiration = jwtTokenProvider.getExpiration(cleanedToken);
        long ttl = expiration - System.currentTimeMillis();

        if (ttl > 0) {
            String blacklistKey = BLACKLIST_TOKEN_PREFIX + cleanedToken;
            redisTemplate.opsForValue().set(blacklistKey, "blacklisted", Duration.ofMillis(ttl));
            log.info("Token has been blacklisted successfully for user: {}", jwtTokenProvider.extractUsername(cleanedToken));
        } else {
            log.warn("Token already expired, no need to blacklist");
        }
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
                            "userName", getDisplayName(user),
                            "resetLink", resetLink,
                            "expiryMinutes", RESET_TOKEN_EXPIRY_MINUTES
                    )
            );
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
        }
    }

    private String getDisplayName(User user) {
        return user.getFullName() != null && !user.getFullName().isBlank()
                ? user.getFullName() : user.getEmail();
    }

    private void logNonExistentEmailAttempt(String email) {
        log.warn("Forgot password attempt for non-existent email: {}", email);
    }

    private void updateUserPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password reset successfully for user ID: {}", user.getId());
    }

    private void sendPasswordChangedNotification(User user) {
        try {
            emailService.sendEmail(
                    user.getEmail(),
                    EmailTemplate.PASSWORD_CHANGED,
                    Map.of(
                            "userName", getDisplayName(user),
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

    private String cleanToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new UnauthorizedException("Token is missing");
        }
        token = token.trim();
        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    private User findUserWithEmail(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private AuthResponse generateAuthResponse(UserPrincipal user) {

        String accessToken = jwtTokenProvider.generateToken(user, !IS_REFRESH_TOKEN);
        String refreshToken = jwtTokenProvider.generateToken(user, IS_REFRESH_TOKEN);

        String roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return AuthResponse.builder()
                .email(user.getEmail())
                .accessToken(accessToken)
                .role(roles)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }
}
