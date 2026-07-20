package com.example.iamservice.domain.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 15:34
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private Long userId;
    private String userKeycloakId;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Boolean isActive;
    private Boolean enabled;
    private Boolean locked;
    private Boolean deleted;
    private Set<RoleSummaryResponse> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
