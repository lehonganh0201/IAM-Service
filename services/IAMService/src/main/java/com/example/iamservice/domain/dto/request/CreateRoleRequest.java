package com.example.iamservice.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 15:40
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Getter
@Setter
public class CreateRoleRequest {

    @NotBlank(message = "Role code is required")
    @Size(min = 2, max = 100, message = "Role code must be between 2 and 100 characters")
    private String code;

    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 150, message = "Role name must be between 2 and 150 characters")
    private String name;

    private String description;

    private Set<String> permissionCodes;
}
