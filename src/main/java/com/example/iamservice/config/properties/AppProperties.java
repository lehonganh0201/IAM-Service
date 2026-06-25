package com.example.iamservice.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 9:40
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private IdentityProvider identityProvider = new IdentityProvider();
    private Jwt jwt = new Jwt();
    private Keycloak keycloak = new Keycloak();
    private Seed seed = new Seed();

    @Getter
    @Setter
    public static class IdentityProvider {
        private IdentityProviderType type = IdentityProviderType.SELF;
    }

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long expirationMs;
        private long refreshExpirationMs;
    }

    @Getter
    @Setter
    public static class Seed {
        private String adminUsername;
        private String adminEmail;
        private String adminPassword;
    }

    @Getter
    @Setter
    public static class Keycloak {
        private String serverUrl;
        private String realm;
        private String issuerUri;
        private String userClientId;
        private String userClientSecret;
        private String adminClientId;
        private String adminClientSecret;
        private String redirectUri;
    }
}
