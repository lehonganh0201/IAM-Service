package com.example.iamservice.client;

import com.example.commonlib.exception.BadRequestException;
import com.example.iamservice.config.properties.AppProperties;
import com.example.iamservice.domain.dto.response.FileUploadResponse;
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
 * Created on:    08/07/2026 at 13:22
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
@RequiredArgsConstructor
public class WebClientStorageClientAdapter implements StorageClientAdapter {

    private final WebClient storageWebClient;
    private final AppProperties appProperties;

    @Retry(name = "storageService")
    @CircuitBreaker(name = "storageService", fallbackMethod = "fallback")
    public FileUploadResponse uploadAvatar(MultipartFile file) {
        try {
            var storage = appProperties.getStorageClient();

            var body = new LinkedMultiValueMap<String, Object>();

            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            body.add("description", "User avatar uploaded by IAM Service");

            String path = "PRIVATE".equalsIgnoreCase(storage.getAvatarVisibility())
                    ? "/api/v1/private/files"
                    : "/api/v1/public/files";

            Map<?, ?> res = storageWebClient.post()
                    .uri(path)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            Map<?, ?> data = res == null ? null : (Map<?, ?>) res.get("data");

            if (data == null) {
                throw new BadRequestException("Invalid Storage response");
            }

            return new FileUploadResponse(
                    UUID.fromString(String.valueOf(data.get("id"))),
                    String.valueOf(data.get("originalName")),
                    String.valueOf(data.get("contentType")),
                    data.get("fileSize") instanceof Number n ? n.longValue() : null
            );

        } catch (Exception e) {
            if (e instanceof BadRequestException b) {
                throw b;
            }

            throw new BadRequestException("Cannot upload avatar");
        }
    }

    public FileUploadResponse fallback(MultipartFile f, Throwable e) {
        throw new BadRequestException("Storage Service is unavailable");
    }

    public void deleteFile(String id) {
        var storage = appProperties.getStorageClient();

        String path = "PRIVATE".equalsIgnoreCase(storage.getAvatarVisibility())
                ? "/api/v1/private/files/{id}"
                : "/api/v1/public/files/{id}";

        storageWebClient.delete()
                .uri(path, id)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}