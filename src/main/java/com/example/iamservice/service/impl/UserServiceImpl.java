package com.example.iamservice.service.impl;

import com.example.iamservice.domain.dto.request.UpdateUserPasswordRequest;
import com.example.iamservice.domain.dto.request.UpdateUserRequest;
import com.example.iamservice.domain.dto.request.UserRequest;
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
import com.example.iamservice.service.UserService;
import com.example.iamservice.util.CloudinaryUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 15:33
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Service
@RequiredArgsConstructor
@Log4j2
public class UserServiceImpl implements UserService {
    private static final String DEFAULT_ROLE = "USER";
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CloudinaryUtil cloudinaryUtil;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse register(UserRequest request) {
        checkEmailExists(request);

        Role role = getRoleDefault();

        User user = buildUser(request, role);

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());

        return buildUserResponse(user);
    }

    @Override
    public UserResponse getMe(String token) {
        String email = extractEmailFromToken(token);

        User user = findUserWithEmail(email);

        return buildUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(String token, UpdateUserRequest request) {
        String email = extractEmailFromToken(token);

        User currentUser = findUserWithEmail(email);

        userMapper.updateUser(request, currentUser);

        uploadFileIfExist(currentUser, request.getAvatar());

        currentUser = userRepository.save(currentUser);

        return buildUserResponse(currentUser);
    }

    @Override
    public UserResponse updateUserPassword(String token, UpdateUserPasswordRequest request) {
        String email = extractEmailFromToken(token);

        User currentUser = findUserWithEmail(email);

        checkPasswordValid(request, currentUser);

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));

        currentUser = userRepository.save(currentUser);

        return buildUserResponse(currentUser);
    }

    private void checkPasswordValid(UpdateUserPasswordRequest request, User currentUser) {
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new BadRequestException("Your old password not matches");
        }

        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
            throw new BadRequestException("Your new password matches with current pass");
        }
    }

    private void uploadFileIfExist(User currentUser, MultipartFile avatar) {
        if (avatar != null && !avatar.isEmpty()) {
            String avatarUrl = cloudinaryUtil.uploadFile(avatar);
            currentUser.setAvatarUrl(avatarUrl);
        }
    }

    private String extractEmailFromToken(String token) {
        String cleanedToken = cleanToken(token);
        if (tokenValidateAndNotRefresh(cleanedToken)) {
            return jwtTokenProvider.extractUsername(cleanedToken);
        }
        throw new UnauthorizedException("Token is not valid");
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

    private boolean tokenValidateAndNotRefresh(String token) {
        return jwtTokenProvider.validateToken(token) && !jwtTokenProvider.isRefreshToken(token);
    }

    private User findUserWithEmail(String username) {
        return userRepository.findByEmail(username)
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

    private void checkEmailExists(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }
    }

    private Role getRoleDefault() {
        return roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new NotFoundException("Role not found"));
    }

    private User buildUser(UserRequest request, Role role) {
        return User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .phoneNumber(request.getPhoneNumber())
                .build();
    }
}
