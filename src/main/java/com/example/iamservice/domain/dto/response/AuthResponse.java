package com.example.iamservice.domain.dto.response;

import lombok.*;

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
@Builder
public class AuthResponse {
    private String email;
    private String accessToken;
    private String refreshToken;
    private String role;
    private String tokenType = "Bearer";
}
