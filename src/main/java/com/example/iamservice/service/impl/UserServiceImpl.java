package com.example.iamservice.service.impl;

import com.example.iamservice.constant.EmailTemplate;
import com.example.iamservice.domain.dto.request.UpdateUserPasswordRequest;
import com.example.iamservice.domain.dto.request.UpdateUserRequest;
import com.example.iamservice.domain.dto.request.UserRequest;
import com.example.iamservice.domain.dto.request.VerifyEmailRequest;
import com.example.iamservice.domain.dto.response.UserResponse;
import com.example.iamservice.domain.entity.Role;
import com.example.iamservice.domain.entity.User;
import com.example.iamservice.domain.mapper.UserMapper;
import com.example.iamservice.exception.BadRequestException;
import com.example.iamservice.exception.ConflictException;
import com.example.iamservice.exception.NotFoundException;
import com.example.iamservice.exception.UnauthorizedException;
import com.example.iamservice.repository.RoleRepository;
import com.example.iamservice.repository.UserRepository;
import com.example.iamservice.security.jwt.JwtTokenProvider;
import com.example.iamservice.service.EmailService;
import com.example.iamservice.service.UserService;
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
 * Description: User Service Implementation - Clean Code
 * ----------------------------------------------------------------------------
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_ROLE = "USER";
    private static final String EMAIL_VERIFICATION_PREFIX = "email_verify:";
    private static final int VERIFICATION_EXPIRY_HOURS = 24;
    private static final int OTP_EXPIRY_MINUTES = 5;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CloudinaryUtil cloudinaryUtil;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;

    @Value("${app.domain-url:http://localhost:8080}")
    private String domainUrl;

    @Override
    @Transactional
    public UserResponse register(UserRequest request) {
        checkEmailExists(request.getEmail());

        Role role = getDefaultRole();
        User user = buildUser(request, role);

        user = userRepository.save(user);
        log.info("User registered successfully with ID: {} | Email: {}", user.getId(), user.getEmail());

        sendRegistrationConfirmationEmail(user);

        return buildUserResponse(user);
    }

    @Override
    public UserResponse getMe(String token) {
        String email = extractEmailFromToken(token);
        User user = findUserByEmail(email);
        return buildUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(String token, UpdateUserRequest request) {
        String email = extractEmailFromToken(token);
        User currentUser = findUserByEmail(email);

        userMapper.updateUser(request, currentUser);
        uploadAvatarIfPresent(currentUser, request.getAvatar());

        currentUser = userRepository.save(currentUser);

        sendProfileUpdatedNotification(currentUser);

        log.info("User profile updated successfully: {}", email);
        return buildUserResponse(currentUser);
    }

    @Override
    @Transactional
    public UserResponse updateUserPassword(String token, UpdateUserPasswordRequest request) {
        String email = extractEmailFromToken(token);
        User currentUser = findUserByEmail(email);

        validatePasswordChange(request, currentUser);

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        currentUser = userRepository.save(currentUser);

        sendPasswordChangedNotification(currentUser);

        log.info("Password updated successfully for user: {}", email);
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

        if (user.isEmailVerified()) {
            log.info("Email already verified for user: {}", user.getEmail());
            return;
        }

        user.setEmailVerified(true);
        userRepository.save(user);

        log.info("Email verified successfully for user: {}", user.getEmail());
    }



    @Override
    public void resendVerificationEmail(String email) {
        userRepository.findByEmail(email.toLowerCase().trim()).ifPresent(user -> {
            if (user.isEmailVerified()) {
                throw new BadRequestException("Email đã được xác thực trước đó");
            }

            String verificationToken = RandomUtil.randomResetToken();
            String redisKey = EMAIL_VERIFICATION_PREFIX + verificationToken;

            redisTemplate.opsForValue().set(redisKey, user.getId().toString(),
                    Duration.ofHours(VERIFICATION_EXPIRY_HOURS));

            String verificationLink = domainUrl + "/verify-email?token=" + verificationToken;

            sendVerificationEmail(user, verificationLink);
            log.info("Verification email resent to: {}", email);
        });
    }

    public void sendTwoFactorOtp(User user, String otpCode) {
        try {
            emailService.sendEmail(
                    user.getEmail(),
                    EmailTemplate.TWO_FACTOR_OTP,
                    Map.of(
                            "userName", getDisplayName(user),
                            "otp", otpCode,
                            "expiryMinutes", OTP_EXPIRY_MINUTES
                    )
            );
            log.info("2FA OTP sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send 2FA OTP to: {}", user.getEmail(), e);
        }
    }

    private void sendRegistrationConfirmationEmail(User user) {
        String verificationToken = RandomUtil.randomResetToken();
        String redisKey = EMAIL_VERIFICATION_PREFIX + verificationToken;

        redisTemplate.opsForValue().set(redisKey, user.getId().toString(),
                Duration.ofHours(VERIFICATION_EXPIRY_HOURS));

        String confirmationLink = domainUrl + "/verify-email?token=" + verificationToken;

        sendVerificationEmail(user, confirmationLink);
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
        if (user.getFullName() != null && !user.getFullName().trim().isEmpty()) {
            return user.getFullName();
        }
        return user.getEmail();
    }

    private void checkEmailExists(String email) {
        if (userRepository.existsByEmail(email.trim().toLowerCase())) {
            throw new ConflictException("Email already exists");
        }
    }

    private Role getDefaultRole() {
        return roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new NotFoundException("Default role 'USER' not found"));
    }

    private User buildUser(UserRequest request, Role role) {
        return User.builder()
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .phoneNumber(request.getPhoneNumber())
                .emailVerified(false)
                .build();
    }

    private String extractEmailFromToken(String token) {
        String cleanedToken = cleanToken(token);
        if (jwtTokenProvider.validateToken(cleanedToken) && !jwtTokenProvider.isRefreshToken(cleanedToken)) {
            return jwtTokenProvider.extractUsername(cleanedToken);
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

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private User findUserById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private UserResponse buildUserResponse(User user) {
        return UserResponse.builder()
                .fullName(user.getFullName())
                .dateOfBirth(user.getDateOfBirth())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    private void validatePasswordChange(UpdateUserPasswordRequest request, User currentUser) {
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new BadRequestException("Old password does not match");
        }
        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }
    }

    private void uploadAvatarIfPresent(User user, MultipartFile avatar) {
        if (avatar != null && !avatar.isEmpty()) {
            String avatarUrl = cloudinaryUtil.uploadFile(avatar);
            user.setAvatarUrl(avatarUrl);
        }
    }
}