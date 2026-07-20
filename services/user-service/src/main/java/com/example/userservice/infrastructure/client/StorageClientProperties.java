package com.example.userservice.infrastructure.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 16:11
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@ConfigurationProperties(prefix = "app.storage-client")
public record StorageClientProperties(String baseUrl, String tokenUri, String clientId, String clientSecret,
                                      String avatarVisibility) {
}
