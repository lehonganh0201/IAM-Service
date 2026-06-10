package com.example.iamservice.domain.dto.response;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

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
    private String fullName;
    private String email;
    private String avatarUrl;
    private String phoneNumber;
    private LocalDate dateOfBirth;
}
