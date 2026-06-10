package com.example.iamservice.domain.mapper;

import com.example.iamservice.domain.dto.request.UpdateUserRequest;
import com.example.iamservice.domain.entity.User;
import org.mapstruct.*;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    10/06/2026 at 8:58
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {
    void updateUser(UpdateUserRequest request, @MappingTarget User user);
}
