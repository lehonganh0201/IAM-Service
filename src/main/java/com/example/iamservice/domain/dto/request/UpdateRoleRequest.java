package com.example.iamservice.domain.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

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
public class UpdateRoleRequest {

    @Size(min = 2, max = 100, message = "Role code must be between 2 and 100 characters")
    private String code;

    @Size(min = 2, max = 150, message = "Role name must be between 2 and 150 characters")
    private String name;

    private String description;
}
