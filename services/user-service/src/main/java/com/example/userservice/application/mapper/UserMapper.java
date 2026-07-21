package com.example.userservice.application.mapper;

import com.example.userservice.application.dto.request.CreateUserRequest;
import com.example.userservice.application.dto.response.UserResponse;
import com.example.userservice.infrastructure.persistence.UserEntity;
import org.mapstruct.Mapper;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 15:24
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserEntity toEntity(CreateUserRequest r);

    UserResponse toResponse(UserEntity e);
}

