package com.example.iamservice.domain.mapper;

import com.example.iamservice.domain.dto.response.RoleSummaryResponse;
import com.example.iamservice.domain.dto.response.UserResponse;
import com.example.iamservice.domain.entity.Role;
import com.example.iamservice.domain.entity.User;
import com.example.iamservice.repository.RoleRepository;
import com.example.iamservice.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    10/06/2026 at 8:58
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    public UserResponse toResponse(User user) {

        Set<Long> roleIds = userRoleRepository.findRoleIdsByUserId(user.getId());

        Set<RoleSummaryResponse> roles =
                roleIds == null ? Set.of()
                        : roleRepository.findAllById(roleIds)
                        .stream()
                        .filter(role -> !Boolean.TRUE.equals(role.getDeleted()))
                        .map(this::toRoleSummary)
                        .collect(Collectors.toSet());

        return new UserResponse(
                user.getId(),
                user.getKeycloakUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getPhoneNumber(),
                user.getDateOfBirth(),
                user.isActive(),
                user.getEnabled(),
                user.getLocked(),
                user.getDeleted(),
                roles,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private RoleSummaryResponse toRoleSummary(Role role) {
        return new RoleSummaryResponse(
                role.getId(),
                role.getCode(),
                role.getName()
        );
    }
}