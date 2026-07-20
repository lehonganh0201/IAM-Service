package com.example.userservice.infrastructure.client;

import com.example.commonlib.exception.BadRequestException;
import com.example.userservice.application.dto.response.FileUploadResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.UUID;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 16:06
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
@RequiredArgsConstructor
public class WebClientStorageClientAdapter implements StorageClientAdapter {
    private final WebClient webClient;
    private final ServiceTokenProvider tokenProvider;
    private final StorageClientProperties storageClientProperties;

    @Retry(name = "storageService")
    @CircuitBreaker(name = "storageService", fallbackMethod = "fallback")
    public FileUploadResponse uploadAvatar(MultipartFile file) {
        try {
            var body = new LinkedMultiValueMap<String, Object>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
            body.add("description", "User avatar uploaded by IAM Service");
            String path = "PRIVATE".equalsIgnoreCase(storageClientProperties.avatarVisibility()) ? "/api/v1/private/files" : "/api/v1/public/files";
            Map<?, ?> res = webClient.post().uri(path).headers(h -> h.setBearerAuth(tokenProvider.getToken())).contentType(MediaType.MULTIPART_FORM_DATA).body(BodyInserters.fromMultipartData(body)).retrieve().bodyToMono(Map.class).block();
            Map<?, ?> data = res == null ? null : (Map<?, ?>) res.get("data");
            if (data == null)
                throw new BadRequestException("Invalid Storage response");
            return new FileUploadResponse(UUID.fromString(String.valueOf(data.get("id"))), String.valueOf(data.get("originalName")), String.valueOf(data.get("contentType")), data.get("fileSize") instanceof Number n ? n.longValue() : null);
        } catch (Exception e) {
            if (e instanceof BadRequestException b) throw b;
            throw new BadRequestException("Cannot upload avatar");
        }
    }

    public FileUploadResponse fallback(MultipartFile f, Throwable e) {
        throw new BadRequestException("Storage Service is unavailable");
    }

    public void deleteFile(String id) {
        webClient.delete().uri("/api/v1/public/files/{id}", id).headers(h -> h.setBearerAuth(tokenProvider.getToken())).retrieve().toBodilessEntity().block();
    }
}
