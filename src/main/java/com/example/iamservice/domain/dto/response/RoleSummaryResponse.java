package com.example.iamservice.domain.dto.response;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 15:06
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */


public record RoleSummaryResponse(
        Long id,
        String code,
        String name
) {
}
