package com.example.iamservice.domain.dto.response;

import lombok.*;

import java.util.UUID;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    08/07/2026 at 11:16
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileResponse {
    private String street;

    private String ward;

    private String district;

    private String province;

    private Double yearsOfExperience;

    private UUID avatarFileId;
}
