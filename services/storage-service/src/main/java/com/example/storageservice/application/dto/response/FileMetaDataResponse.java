package com.example.storageservice.application.dto.response;

import com.example.storageservice.domain.model.FileStatus;
import com.example.storageservice.domain.model.FileVisibility;
import com.example.storageservice.domain.model.StorageProvider;

import java.time.Instant;
import java.util.UUID;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 11:52
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public record FileMetaDataResponse(UUID id, String originalName, String extension, String contentType, Long fileSize,
                                   String checksumSha256, StorageProvider storageProvider, FileVisibility visibility,
                                   String ownerId, String ownerUsername, String description, String tags,
                                   Integer imageWidth, Integer imageHeight, Instant createdAt, Instant updatedAt,
                                   Long version, FileStatus status) {
}
