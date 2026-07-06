package com.example.storageservice.config;

import com.example.storageservice.domain.model.StorageProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 11:25
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(StorageProvider provider, long maxFileSizeBytes, Set<String> allowedExtensions,
                                Set<String> allowedMimeTypes, boolean allowAnonymousPublicDownload, String localRoot,
                                Minio minio, boolean hardDelete) {
    public record Minio(String endpoint, String accessKey, String secretKey, String bucket) {
    }
}
