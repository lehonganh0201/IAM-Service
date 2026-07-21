package com.example.iamservice.client;

import com.example.iamservice.config.properties.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServletBearerExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    08/07/2026 at 13:37
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Configuration
@RequiredArgsConstructor
public class StorageWebClientConfig {

    private final AppProperties appProperties;

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient storageWebClient(WebClient.Builder builder) {
        var storage = appProperties.getStorageClient();

        ServletBearerExchangeFilterFunction bearer =
                new ServletBearerExchangeFilterFunction();

        return builder
                .baseUrl(storage.getBaseUrl())
                .filter(bearer)
                .build();
    }
}