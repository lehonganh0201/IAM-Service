package com.example.iamservice.security;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 10:24
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */


public record IamPrincipal(
        Long userId,
        String username,
        String email,
        String identityProvider
) {
}
