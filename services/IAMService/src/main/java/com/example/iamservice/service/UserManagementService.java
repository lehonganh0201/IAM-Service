package com.example.iamservice.service;

import com.example.commonlib.api.PageResponse;
import com.example.iamservice.domain.dto.request.AssignUserRolesRequest;
import com.example.iamservice.domain.dto.request.CreateUserRequest;
import com.example.iamservice.domain.dto.request.ResetUserPasswordRequest;
import com.example.iamservice.domain.dto.request.UpdateUserRequest;
import com.example.iamservice.domain.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 15:17
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */


public interface UserManagementService {
    PageResponse<UserResponse> getUsers(String keyword, Pageable pageable);

    UserResponse getUserById(Long id);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    UserResponse lockUser(Long id);

    UserResponse unlockUser(Long id);

    UserResponse assignRoles(Long id, AssignUserRolesRequest request);

    void resetPassword(Long id, ResetUserPasswordRequest request);

    void deleteUser(Long id, String reason);

    UserResponse uploadAvatar(Long id, MultipartFile file);

    UserResponse deleteAvatar(Long id);
}
