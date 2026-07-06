package com.example.userservice.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 15:40
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public record UpdateUserRequest(@Size(max = 255) String fullName, @PastOrPresent LocalDate dateOfBirth,
                                @Size(max = 255) String street, @Size(max = 255) String ward,
                                @Size(max = 255) String district, @Size(max = 255) String province,
                                @Min(0) Double yearsOfExperience) {
}

