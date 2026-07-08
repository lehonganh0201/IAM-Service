package com.example.storageservice.infrastructure.persistence;

import com.example.storageservice.domain.model.FileStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 11:38
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public interface FileMetadataRepository extends JpaRepository<FileMetadataEntity, UUID>, JpaSpecificationExecutor<FileMetadataEntity> {
    Optional<FileMetadataEntity> findByIdAndStatus(UUID id, FileStatus status);
}
