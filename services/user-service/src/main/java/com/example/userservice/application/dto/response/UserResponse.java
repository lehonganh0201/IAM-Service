package com.example.userservice.application.dto.response;

import com.example.userservice.domain.model.UserStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 15:18
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public record UserResponse(UUID id, String username, String fullName, LocalDate dateOfBirth, String street, String ward,
                           String district, String province, Double yearsOfExperience, UUID avatarFileId,
                           Instant createdAt, Instant updatedAt, UserStatus status) {
}
