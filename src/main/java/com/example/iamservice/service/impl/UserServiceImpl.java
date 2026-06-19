package com.example.iamservice.service.impl;

import com.example.iamservice.config.properties.AppProperties;
import com.example.iamservice.config.properties.IdentityProviderType;
import com.example.iamservice.constant.AuditAction;
import com.example.iamservice.constant.AuditResult;
import com.example.iamservice.constant.EmailTemplate;
import com.example.iamservice.domain.dto.request.*;
import com.example.iamservice.domain.dto.response.KeycloakUserProvisioningResult;
import com.example.iamservice.domain.dto.response.UserResponse;
import com.example.iamservice.domain.entity.User;
import com.example.iamservice.exception.BadRequestException;
import com.example.iamservice.exception.ConflictException;
import com.example.iamservice.exception.NotFoundException;
import com.example.iamservice.exception.UnauthorizedException;
import com.example.iamservice.repository.UserRepository;
import com.example.iamservice.security.jwt.JwtTokenProvider;
import com.example.iamservice.service.AuditLogService;
import com.example.iamservice.service.EmailService;
import com.example.iamservice.service.UserService;
import com.example.iamservice.service.keycloak.KeycloakAdminService;
import com.example.iamservice.util.CloudinaryUtil;
import com.example.iamservice.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * ----------------------------------------------------------------------------
 * Author: Hong Anh
 * Created on: 10/06/2026
 * Project: IAMService
 * ----------------------------------------------------------------------------
 */

@Service
@RequiredArgsConstructor
@Log4j2
public class UserServiceImpl implements UserService {

    private static final String EMAIL_VERIFICATION_PREFIX = "email_verify:";
    private static final int VERIFICATION_EXPIRY_HOURS = 24;
    private static final int OTP_EXPIRY_MINUTES = 5;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CloudinaryUtil cloudinaryUtil;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;
    private final UserProfileCacheService userProfileCacheService;
    private final AppProperties appProperties;
    private final KeycloakAdminService keycloakAdminService;
    private final AuditLogService auditLogService;

    @Value("${app.domain-url:http://localhost:8080}")
    private String domainUrl;

    @Override
    @Transactional
    public UserResponse register(UserRequest request) {
        if (appProperties.getIdentityProvider().getType() == IdentityProviderType.KEYCLOAK) {
            return registerWithKeycloak(request);
        }

        return registerWithSelfIdp(request);
    }

    @Override
    public UserResponse getMe(String token) {
        String username = extractUsernameFromToken(token);
        return userProfileCacheService.getUserProfileByEmail(username);
    }

    @Override
    @Transactional
    public UserResponse updateUser(String token, UpdateUserRequest request) {
        String username = extractUsernameFromToken(token);
        return updateUserByEmail(username, request);
    }

    @Override
    @Transactional
    public UserResponse updateUserPassword(String token, UpdateUserPasswordRequest request) {
        String username = extractUsernameFromToken(token);
        User currentUser = findUserByUsername(username);

        validatePasswordChange(request, currentUser);

        currentUser.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        currentUser = userRepository.save(currentUser);

        sendPasswordChangedNotification(currentUser);

        log.info("Password updated successfully for user: {}", username);
        return buildUserResponse(currentUser);
    }

    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        String token = request.getToken().trim();
        String redisKey = EMAIL_VERIFICATION_PREFIX + token;

        String userIdStr = redisTemplate.opsForValue().getAndDelete(redisKey);
        if (userIdStr == null) {
            throw new UnauthorizedException("Token xác thực email không hợp lệ hoặc đã hết hạn");
        }

        User user = findUserById(Long.parseLong(userIdStr));

        if (user.getEnabled()) {
            log.info("Email already verified for user: {}", user.getEmail());
            return;
        }

        user.setEnabled(true);
        userRepository.save(user);

        log.info("Email verified successfully for user: {}", user.getEmail());
    }



    @Override
    public void resendVerificationEmail(String email) {
        userRepository.findByEmailAndDeletedFalse(email.toLowerCase().trim()).ifPresent(user -> {
            if (user.getEnabled()) {
                throw new BadRequestException("Email đã được xác thực trước đó");
            }

            String verificationToken = RandomUtil.randomResetToken();
            String redisKey = EMAIL_VERIFICATION_PREFIX + verificationToken;

            redisTemplate.opsForValue().set(redisKey, user.getId().toString(),
                    Duration.ofHours(VERIFICATION_EXPIRY_HOURS));

            String verificationLink = buildVerifyUrl(verificationToken);

            sendVerificationEmail(user, verificationLink);
            log.info("Verification email resent to: {}", email);
        });
    }

    public UserResponse updateUserByEmail(String email, UpdateUserRequest request) {
        User currentUser = findUserByUsername(email);

        updateUser(request, currentUser);
        uploadAvatarIfPresent(currentUser, request.getAvatar());

        currentUser = userRepository.save(currentUser);

        userProfileCacheService.evictUserProfile(email);

        sendProfileUpdatedNotification(currentUser);

        return buildUserResponse(currentUser);
    }

    public void updateUser(UpdateUserRequest request, User user) {
        if ( request == null ) {
            return;
        }

        if ( request.getFirstName() != null ) {
            user.setFirstName( request.getFirstName() );
        }
        if ( request.getLastName() != null ) {
            user.setLastName( request.getLastName() );
        }
        if ( request.getPhoneNumber() != null ) {
            user.setPhoneNumber( request.getPhoneNumber() );
        }
        if ( request.getDateOfBirth() != null ) {
            user.setDateOfBirth( request.getDateOfBirth() );
        }
    }

    /*private void sendRegistrationConfirmationEmail(User user) {
        String verificationToken = RandomUtil.randomResetToken();
        String redisKey = EMAIL_VERIFICATION_PREFIX + verificationToken;

        redisTemplate.opsForValue().set(redisKey, user.getId().toString(),
                Duration.ofHours(VERIFICATION_EXPIRY_HOURS));

        String confirmationLink = buildVerifyUrl(verificationToken);

        sendVerificationEmail(user, confirmationLink);
    }*/

    private String buildVerifyUrl(String verificationToken) {
        return domainUrl + "/verify-email?token=" + verificationToken;
    }

    private void sendVerificationEmail(User user, String confirmationLink) {
        try {
            emailService.sendEmail(
                    user.getEmail(),
                    EmailTemplate.REGISTRATION_CONFIRMATION,
                    Map.of(
                            "userName", getDisplayName(user),
                            "confirmationLink", confirmationLink,
                            "expiryHours", VERIFICATION_EXPIRY_HOURS
                    )
            );
        } catch (Exception e) {
            log.warn("Failed to send verification email to: {}", user.getEmail(), e);
        }
    }

    private void sendProfileUpdatedNotification(User user) {
        try {
            emailService.sendEmail(
                    user.getEmail(),
                    EmailTemplate.PROFILE_UPDATED,
                    Map.of(
                            "userName", getDisplayName(user),
                            "email", user.getEmail(),
                            "updatedAt", LocalDateTime.now()
                    )
            );
        } catch (Exception e) {
            log.warn("Failed to send profile updated notification to: {}", user.getEmail(), e);
        }
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

    private String getDisplayName(User user) {
        if (user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty()) {
            return user.getDisplayName();
        }
        return user.getEmail();
    }

    private String extractUsernameFromToken(String token) {
        String cleanedToken = cleanToken(token);
        if (jwtTokenProvider.validateAccessToken(cleanedToken)) {
            return jwtTokenProvider.getUsername(cleanedToken);
        }
        throw new UnauthorizedException("Token is not valid");
    }

    private String cleanToken(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Token is missing");
        }
        token = token.trim();
        return token.startsWith("Bearer ") ? token.substring(7) : token;
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private User findUserById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private UserResponse buildUserResponse(User user) {
        return UserResponse.builder()
                .fullName(user.getDisplayName())
                .dateOfBirth(user.getDateOfBirth())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    private void validatePasswordChange(UpdateUserPasswordRequest request, User currentUser) {
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPasswordHash())) {
            throw new BadRequestException("Old password does not match");
        }
        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPasswordHash())) {
            throw new BadRequestException("New password must be different from current password");
        }
    }

    private void uploadAvatarIfPresent(User user, MultipartFile avatar) {
        if (avatar != null && !avatar.isEmpty()) {
            String avatarUrl = cloudinaryUtil.uploadFile(avatar);
            user.setAvatarUrl(avatarUrl);
        }
    }

    private UserResponse registerWithKeycloak(UserRequest request) {
        validateUniqueUser(request.getUsername(), request.getEmail());

        KeycloakUserProvisioningResult provisionedUser = null;

        try {
            provisionedUser = keycloakAdminService.createUser(
                    new KeycloakRegisterRequest(
                            request.getUsername(),
                            request.getEmail(),
                            request.getFirstName(),
                            request.getLastName(),
                            request.getPassword()
                    )
            );

            User user = User.builder()
                    .keycloakUserId(provisionedUser.keycloakUserId())
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .passwordHash(null)
                    .passwordHash(null)
                    .enabled(true)
                    .locked(false)
                    .build();

            User savedUser = userRepository.save(user);

            auditLogSuccess(AuditAction.AUTH_REGISTER, savedUser.getUsername(), savedUser.getId());

            return mapToResponse(savedUser);

        } catch (Exception exception) {
            if (provisionedUser != null && provisionedUser.keycloakUserId() != null) {
                rollbackCreatedKeycloakUser(provisionedUser.keycloakUserId());
            }

            throw exception;
        }
    }

    private void rollbackCreatedKeycloakUser(String keycloakUserId) {
        try {
            keycloakAdminService.disableUser(keycloakUserId);
        } catch (Exception ignored) {
            log.warn("Register user with keycloak fail with id {}", keycloakUserId);
        }
    }

    private UserResponse registerWithSelfIdp(UserRequest request) {
        validateUniqueUser(request.getUsername(), request.getEmail());

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .passwordHash(encodedPassword)
                .passwordHash(encodedPassword)
                .enabled(true)
                .locked(false)
                .build();

        User savedUser = userRepository.save(user);

        auditLogSuccess(AuditAction.AUTH_REGISTER, savedUser.getUsername(), savedUser.getId());

        return mapToResponse(savedUser);
    }

    private UserResponse mapToResponse(User savedUser) {
        return UserResponse.builder()
                .fullName(savedUser.getDisplayName())
                .email(savedUser.getEmail())
                .phoneNumber(savedUser.getPhoneNumber())
                .dateOfBirth(savedUser.getDateOfBirth())
                .avatarUrl(savedUser.getAvatarUrl())
                .username(savedUser.getUsername())
                .userId(savedUser.getId())
                .userKeycloakId(savedUser.getKeycloakUserId())
                .isActive(savedUser.isActive())
                .build();
    }

    private void validateUniqueUser(String username, String email) {
        if (userRepository.existsByUsernameAndDeletedFalse(username)) {
            auditLogFailure(AuditAction.AUTH_REGISTER, username, "Username already exists");
            throw new ConflictException("Username already exists");
        }

        if (userRepository.existsByEmailAndDeletedFalse(email)) {
            auditLogFailure(AuditAction.AUTH_REGISTER, email, "Email already exists");
            throw new ConflictException("Email already exists");
        }
    }

    private void auditLogFailure(AuditAction action, String usernameOrEmail, String errorMessage) {
        auditLogService.saveAuthAudit(
                action,
                AuditResult.FAILURE,
                usernameOrEmail,
                null,
                errorMessage
        );
    }

    private void auditLogSuccess(AuditAction action, String usernameOrEmail, Long userId) {
        auditLogService.saveAuthAudit(
                action,
                AuditResult.SUCCESS,
                usernameOrEmail,
                userId,
                null
        );
    }
}