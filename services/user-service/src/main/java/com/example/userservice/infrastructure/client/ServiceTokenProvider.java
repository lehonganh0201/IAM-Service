package com.example.userservice.infrastructure.client;

import com.example.commonlib.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Map;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 16:15
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
@RequiredArgsConstructor
public class ServiceTokenProvider {
    private final StorageClientProperties storageClientProperties;
    private final WebClient webClient;
    private volatile String token;
    private volatile Instant exp = Instant.EPOCH;

    public synchronized String getToken() {
        if (token != null && Instant.now().isBefore(exp.minusSeconds(30))) return token;
        var f = new LinkedMultiValueMap<String, String>();
        f.add("grant_type", "client_credentials");
        f.add("client_id", storageClientProperties.clientId());
        f.add("client_secret", storageClientProperties.clientSecret());
        Map<?, ?> r = webClient.post().uri(storageClientProperties.tokenUri()).contentType(MediaType.APPLICATION_FORM_URLENCODED).body(BodyInserters.fromFormData(f)).retrieve().bodyToMono(Map.class).block();
        if (r == null || r.get("access_token") == null)
            throw new BadRequestException("Cannot get service token");
        token = String.valueOf(r.get("access_token"));
        exp = Instant.now().plusSeconds(r.get("expires_in") instanceof Number n ? n.longValue() : 300);
        return token;
    }
}
