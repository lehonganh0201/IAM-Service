package com.example.iamservice.domain.dto.response.common;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 16:03
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public record FieldErrorResponse(
        String field,
        String message
) {
}
