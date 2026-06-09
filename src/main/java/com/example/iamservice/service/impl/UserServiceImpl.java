package com.example.iamservice.service.impl;

import com.example.iamservice.domain.dto.request.UserRequest;
import com.example.iamservice.domain.dto.response.UserResponse;
import com.example.iamservice.domain.entity.Role;
import com.example.iamservice.domain.entity.User;
import com.example.iamservice.exception.ConflictException;
import com.example.iamservice.exception.NotFoundException;
import com.example.iamservice.repository.RoleRepository;
import com.example.iamservice.repository.UserRepository;
import com.example.iamservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public UserResponse register(UserRequest request) {
        checkEmailExists(request);

        Role role = getRoleDefault();

        User user = buildUser(request, role);

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());

        return UserResponse.builder()
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
                .build();
    }
}
