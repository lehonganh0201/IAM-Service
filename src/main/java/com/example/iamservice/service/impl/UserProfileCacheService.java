package com.example.iamservice.service.impl;

import com.example.iamservice.domain.dto.response.UserResponse;
import com.example.iamservice.domain.entity.User;
import com.example.iamservice.exception.NotFoundException;
import com.example.iamservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    12/06/2026 at 11:08
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Service
@RequiredArgsConstructor
public class UserProfileCacheService {

    private final UserRepository userRepository;

    @Cacheable(value = "users", key = "#username")
    public UserResponse getUserProfileByEmail(String username) {
        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return UserResponse.builder()
                .fullName(user.getDisplayName())
                .dateOfBirth(user.getDateOfBirth())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    @CacheEvict(value = "users", key = "#email")
    public void evictUserProfile(String email) {
    }
}
