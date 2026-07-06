package com.example.userservice.application.usecase;

import com.example.commonlib.api.common.PageResponse;
import com.example.commonlib.exception.ConflictException;
import com.example.commonlib.exception.NotFoundException;
import com.example.userservice.application.dto.request.CreateUserRequest;
import com.example.userservice.application.dto.request.UpdateUserRequest;
import com.example.userservice.application.dto.request.UserSearchQuery;
import com.example.userservice.application.dto.response.UserResponse;
import com.example.userservice.application.mapper.UserMapper;
import com.example.userservice.domain.model.UserStatus;
import com.example.userservice.infrastructure.persistence.UserEntity;
import com.example.userservice.infrastructure.persistence.UserRepository;
import com.example.userservice.infrastructure.persistence.UserSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

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

    public UserResponse get(UUID id) {
        return userMapper.toResponse(find(id));
    }

    public PageResponse<UserResponse> search(UserSearchQuery q, Pageable p) {
        return PageResponse.from(
                userRepository.findAll(
                                UserSpecifications.byQuery(q), PageRequest.of(p.getPageNumber(), p.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt")))
                        .map(userMapper::toResponse));
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest r) {
        var e = find(id);

        if (r.fullName() != null) e.setFullName(r.fullName());
        if (r.dateOfBirth() != null) e.setDateOfBirth(r.dateOfBirth());
        if (r.street() != null) e.setStreet(r.street());
        if (r.ward() != null) e.setWard(r.ward());
        if (r.district() != null) e.setDistrict(r.district());
        if (r.province() != null) e.setProvince(r.province());
        if (r.yearsOfExperience() != null) e.setYearsOfExperience(r.yearsOfExperience());
        e.setUpdatedAt(Instant.now());

        return userMapper.toResponse(userRepository.save(e));
    }

    @Transactional
    public void delete(UUID id) {
        var e = find(id);
        e.setStatus(UserStatus.DELETED);
        e.setUpdatedAt(Instant.now());
        userRepository.save(e);
    }

    private UserEntity find(UUID id) {
        return userRepository.findByIdAndStatusNot(id, UserStatus.DELETED).orElseThrow(() -> new NotFoundException("User not found"));
    }
}
