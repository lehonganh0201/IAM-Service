package com.example.iamservice.domain.entity;

import com.example.iamservice.domain.entity.common.DateAuditing;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    08/07/2026 at 10:59
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfile extends DateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String street;

    private String ward;

    private String district;

    private String province;

    private Double yearsOfExperience;

    private UUID avatarFileId;
}
