package com.example.userservice.application.usecase;

import com.example.userservice.application.dto.response.UserResponse;
import com.example.userservice.application.mapper.UserMapper;
import com.example.userservice.infrastructure.client.StorageClientAdapter;
import com.example.userservice.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 15:50
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Service
@RequiredArgsConstructor
public class AvatarUseCase {
    private final UserUseCases users;
    private final StorageClientAdapter storageAdapter;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse uploadAvatar(UUID id, MultipartFile file) {
        var u = users.find(id);
        var f = storageAdapter.uploadAvatar(file);

        u.setAvatarFileId(f.id());
        u.setUpdatedAt(Instant.now());

        return userMapper.toResponse(userRepository.save(u));
    }
}
