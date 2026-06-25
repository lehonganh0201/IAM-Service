package com.example.iamservice.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 14:36
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;

    @NotNull(message = "First name is required")
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotNull(message = "Last name is required")
    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email
    @NotNull(message = "Email is required")
    @NotBlank(message = "Email is required")
    private String email;

    @NotNull(message = "Password is required")
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    private String password;
}
