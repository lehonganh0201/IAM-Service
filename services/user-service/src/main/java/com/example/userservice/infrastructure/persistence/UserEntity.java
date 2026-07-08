package com.example.userservice.infrastructure.persistence;

import com.example.userservice.domain.model.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 15:21
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Getter
@Setter
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private LocalDate dateOfBirth;

    private String street;

    private String ward;

    private String district;

    private String province;

    private Double yearsOfExperience;

    private UUID avatarFileId;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @PrePersist
    void pre() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = UserStatus.ACTIVE;
    }
}

