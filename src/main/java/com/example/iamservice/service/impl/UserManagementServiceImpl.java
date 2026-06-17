package com.example.iamservice.service.impl;

import com.example.iamservice.base.PageResponse;
import com.example.iamservice.config.properties.AppProperties;
import com.example.iamservice.config.properties.IdentityProviderType;
import com.example.iamservice.domain.dto.request.*;
import com.example.iamservice.domain.dto.response.KeycloakUserProvisioningResult;
import com.example.iamservice.domain.dto.response.UserResponse;
import com.example.iamservice.domain.entity.Role;
import com.example.iamservice.domain.entity.User;
import com.example.iamservice.domain.mapper.UserMapper;
import com.example.iamservice.repository.RoleRepository;
import com.example.iamservice.repository.UserRepository;
import com.example.iamservice.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 15:18
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {
    private final AppProperties appProperties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final KeycloakAdminService keycloakAdminService;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsers(String keyword, Pageable pageable) {
        Page<User> users;

        if (StringUtils.hasText(keyword)) {
            users = userRepository.searchActiveUsersForList(keyword, pageable);
        } else {
            users = userRepository.findByDeletedFalse(pageable);
        }

        Page<UserResponse> responsePage = users.map(userMapper::toResponse);

        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = getActiveUser(id);

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        validateUniqueUser(request.getUsername(), request.getEmail());

        if (isKeycloakMode()) {
            return createUserWithKeycloak(request);
        }

        return createUserWithSelfIdp(request);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = getActiveUser(id);

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDateOfBirth(request.getDateOfBirth());

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        if (isKeycloakMode() && StringUtils.hasText(user.getKeycloakUserId())) {
            keycloakAdminService.updateUserProfile(
                    user.getKeycloakUserId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEnabled()
            );
        }

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse lockUser(Long id) {
        User user = getActiveUser(id);

        user.setLocked(true);

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse unlockUser(Long id) {
        User user = getActiveUser(id);

        user.setLocked(false);

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse assignRoles(Long id, AssignUserRolesRequest request) {
        User user = getActiveUser(id);

        List<Role> roles = roleRepository.findByCodeInAndDeletedFalse(request.getRoleCodes());

        if (roles.size() != request.getRoleCodes().size()) {
            throw new IllegalArgumentException("Some roles do not exist or have been deleted");
        }

        user.setRoles(new HashSet<>(roles));

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public void resetPassword(Long id, ResetUserPasswordRequest request) {
        User user = getActiveUser(id);

        if (isKeycloakMode()) {
            if (!StringUtils.hasText(user.getKeycloakUserId())) {
                throw new IllegalStateException("User is not linked with Keycloak");
            }

            boolean temporary = Boolean.TRUE.equals(request.getTemporary());

            keycloakAdminService.resetPassword(
                    user.getKeycloakUserId(),
                    request.getNewPassword(),
                    temporary
            );

            return;
        }

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());

        user.setPasswordHash(encodedPassword);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getActiveUser(id);

        user.setDeleted(true);
        user.setEnabled(false);
        user.setLocked(true);

        if (isKeycloakMode() && StringUtils.hasText(user.getKeycloakUserId())) {
            keycloakAdminService.disableUser(user.getKeycloakUserId());
        }

        userRepository.save(user);
    }

    private UserResponse createUserWithSelfIdp(CreateUserRequest request) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth())
                .passwordHash(encodedPassword)
                .enabled(true)
                .locked(false)
                .deleted(false)
                .roles(resolveRoles(request.getRoleCodes()))
                .build();

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    private UserResponse createUserWithKeycloak(CreateUserRequest request) {
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
                    .phoneNumber(request.getPhoneNumber())
                    .dateOfBirth(request.getDateOfBirth())
                    .passwordHash(null)
                    .enabled(true)
                    .locked(false)
                    .deleted(false)
                    .roles(resolveRoles(request.getRoleCodes()))
                    .build();

            User savedUser = userRepository.save(user);

            return userMapper.toResponse(savedUser);

        } catch (Exception exception) {
            if (provisionedUser != null && StringUtils.hasText(provisionedUser.keycloakUserId())) {
                rollbackCreatedKeycloakUser(provisionedUser.keycloakUserId());
            }

            throw exception;
        }
    }

    private Set<Role> resolveRoles(Set<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return Set.of();
        }

        List<Role> roles = roleRepository.findByCodeInAndDeletedFalse(roleCodes);

        if (roles.size() != roleCodes.size()) {
            throw new IllegalArgumentException("Some roles do not exist or have been deleted");
        }

        return new HashSet<>(roles);
    }

    private void rollbackCreatedKeycloakUser(String keycloakUserId) {
        try {
            keycloakAdminService.disableUser(keycloakUserId);
        } catch (Exception ignored) {
        }
    }

    private User getActiveUser(Long id) {
        return userRepository.findWithRolesByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private void validateUniqueUser(String username, String email) {
        if (userRepository.existsByUsernameAndDeletedFalse(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmailAndDeletedFalse(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
    }

    private boolean isKeycloakMode() {
        return appProperties.getIdentityProvider().getType() == IdentityProviderType.KEYCLOAK;
    }
}