package com.example.iamservice.domain.dto.response;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 13:18
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */


public record KeycloakUserProvisioningResult(
        String keycloakUserId,
        String username,
        String email
) {
}
