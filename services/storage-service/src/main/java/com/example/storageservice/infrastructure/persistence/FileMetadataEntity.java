package com.example.storageservice.infrastructure.persistence;

import com.example.storageservice.domain.model.FileStatus;
import com.example.storageservice.domain.model.FileVisibility;
import com.example.storageservice.domain.model.StorageProvider;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 11:36
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Getter
@Setter
@Entity
@Table(name = "file_metadata")
public class FileMetadataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "stored_name", nullable = false, unique = true)
    private String storedName;

    @Column(nullable = false, length = 32)
    private String extension;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "checksum_sha256", nullable = false, length = 64)
    private String checksumSha256;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_provider", nullable = false)
    private StorageProvider storageProvider;

    @Column(name = "bucket_name")
    private String bucketName;

    @Column(name = "object_key", nullable = false, length = 1000)
    private String objectKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileVisibility visibility;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(name = "owner_username", nullable = false)
    private String ownerUsername;

    @Column(length = 1000)
    private String description;

    @Column(length = 1000)
    private String tags;

    private Integer imageWidth;

    private Integer imageHeight;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    private Instant updatedAt;
    private String updatedBy;
    private Instant deletedAt;
    private String deletedBy;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileStatus status;

    @PrePersist
    void pre() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = FileStatus.ACTIVE;
    }
}
