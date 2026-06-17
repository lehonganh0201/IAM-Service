package com.example.iamservice.domain.dto.request;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 13:17
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */


public record KeycloakRegisterRequest(
        String username,
        String email,
        String firstName,
        String lastName,
        String password
) {
}
