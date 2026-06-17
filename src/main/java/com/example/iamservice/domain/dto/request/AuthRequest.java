package com.example.iamservice.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 17:04
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {
    @Email
    @NotNull(message = "Email is required")
    @NotBlank(message = "Email is required")
    private String usernameOrEmail;

    @NotNull(message = "Password is required")
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    private String password;
}
