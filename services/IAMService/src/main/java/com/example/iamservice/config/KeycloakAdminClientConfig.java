package com.example.iamservice.config;

import com.example.iamservice.config.properties.AppProperties;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 13:22
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Configuration
@RequiredArgsConstructor
public class KeycloakAdminClientConfig {

    private final AppProperties appProperties;

    @Bean
    public Keycloak keycloakAdminClient() {
        AppProperties.Keycloak keycloak = appProperties.getKeycloak();

        return KeycloakBuilder.builder()
                .serverUrl(keycloak.getServerUrl())
                .realm(keycloak.getRealm())
                .clientId(keycloak.getAdminClientId())
                .clientSecret(keycloak.getAdminClientSecret())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }
}
