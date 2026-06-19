package com.example.iamservice.service.keycloak;

import com.example.iamservice.config.properties.AppProperties;
import com.example.iamservice.domain.dto.response.KeycloakTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 13:20
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Service
@RequiredArgsConstructor
public class KeycloakUserService {

    private final AppProperties appProperties;
    private final RestClient restClient = RestClient.create();

    public String buildAuthorizationUrl(String provider) {
        AppProperties.Keycloak keycloak = appProperties.getKeycloak();

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUri(URI.create(keycloak.getServerUrl()))
                .pathSegment(
                        "realms",
                        keycloak.getRealm(),
                        "protocol",
                        "openid-connect",
                        "auth"
                )
                .queryParam("client_id", keycloak.getUserClientId())
                .queryParam("response_type", "code")
                .queryParam("scope", "openid profile email")
                .queryParam("redirect_uri", keycloak.getRedirectUri());

        if (provider != null && !provider.isBlank()) {
            if ("google".equals(provider)) {
                builder.queryParam("kc_idp_hint", provider);
            }
        }

        return builder
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();
    }

    public KeycloakTokenResponse refresh(String refreshToken) {
        AppProperties.Keycloak keycloak = appProperties.getKeycloak();

        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", keycloak.getUserClientId());
        form.add("refresh_token", refreshToken);

        if (keycloak.getUserClientSecret() != null && !keycloak.getUserClientSecret().trim().isBlank()) {
            form.add("client_secret", keycloak.getUserClientSecret());
        }

        return restClient.post()
                .uri(tokenEndpoint())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(KeycloakTokenResponse.class);
    }

    public void logout(String refreshToken) {
        AppProperties.Keycloak keycloak = appProperties.getKeycloak();

        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", keycloak.getUserClientId());
        form.add("refresh_token", refreshToken);

        if (keycloak.getUserClientSecret() != null && !keycloak.getUserClientSecret().trim().isBlank()) {
            form.add("client_secret", keycloak.getUserClientSecret());
        }

        restClient.post()
                .uri(logoutEndpoint())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toBodilessEntity();
    }

    public KeycloakTokenResponse exchangeCode(
            String code,
            String redirectUri,
            String codeVerifier
    ) {
        AppProperties.Keycloak keycloak = appProperties.getKeycloak();

        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();

        form.add("grant_type", "authorization_code");
        form.add("client_id", keycloak.getUserClientId());
        form.add("code", code);
        form.add("redirect_uri", redirectUri);

        if (StringUtils.hasText(keycloak.getUserClientSecret().trim())) {
            form.add("client_secret", keycloak.getUserClientSecret());
        }

        if (StringUtils.hasText(codeVerifier)) {
            form.add("code_verifier", codeVerifier);
        }

        return restClient.post()
                .uri(tokenEndpoint())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(KeycloakTokenResponse.class);
    }

    private String tokenEndpoint() {
        AppProperties.Keycloak keycloak = appProperties.getKeycloak();

        return keycloak.getServerUrl()
                + "/realms/"
                + keycloak.getRealm()
                + "/protocol/openid-connect/token";
    }

    private String logoutEndpoint() {
        AppProperties.Keycloak keycloak = appProperties.getKeycloak();

        return keycloak.getServerUrl()
                + "/realms/"
                + keycloak.getRealm()
                + "/protocol/openid-connect/logout";
    }
}
