package com.example.userservice.application.usecase;

import com.example.commonlib.exception.ConflictException;
import com.example.userservice.application.dto.request.CreateUserRequest;
import com.example.userservice.application.dto.response.UserResponse;
import com.example.userservice.application.mapper.UserMapper;
import com.example.userservice.domain.model.UserStatus;
import com.example.userservice.infrastructure.persistence.UserEntity;
import com.example.userservice.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 15:16
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Service
@RequiredArgsConstructor
public class UserUseCases {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse create(CreateUserRequest r) {
        if (userRepository.existsByUsernameAndStatusNot(r.username(), UserStatus.DELETED))
            throw new ConflictException("Username already exists");
        UserEntity e = userMapper.toEntity(r);
        e.setStatus(UserStatus.ACTIVE);
        return userMapper.toResponse(userRepository.save(e));
    }
}
