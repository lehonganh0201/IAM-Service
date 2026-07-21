package com.example.iamservice.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    08/07/2026 at 13:25
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public record StorageClientProperties(String baseUrl, String tokenUri, String clientId, String clientSecret,
                                      String avatarVisibility) {
}