package com.example.iamservice.domain.entity;

import com.example.iamservice.domain.entity.common.SoftDeleteAuditing;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 13:13
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User extends SoftDeleteAuditing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private String keycloakUserId;
    private String username;
    private String email;
    private String passwordHash;

    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean locked = false;

    private String phoneNumber;

    private LocalDate dateOfBirth;

    private String avatarUrl;

    private Long profileId;

    public boolean isActive() {
        return Boolean.TRUE.equals(enabled)
                && !Boolean.TRUE.equals(locked)
                && !Boolean.TRUE.equals(getDeleted());
    }

    public String getDisplayName() {
        String first = firstName == null ? "" : firstName;
        String last = lastName == null ? "" : lastName;
        String fullName = (first + " " + last).trim();
        return fullName.isBlank() ? email : fullName;
    }
}
