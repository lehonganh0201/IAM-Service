package com.example.storageservice.application.usecase;

import com.example.commonlib.exception.BadRequestException;
import com.example.commonlib.exception.ForbiddenException;
import com.example.commonlib.security.CurrentUser;
import com.example.storageservice.application.dto.response.FileMetaDataResponse;
import com.example.storageservice.application.mapper.FileMetadataMapper;
import com.example.storageservice.application.service.ChecksumService;
import com.example.storageservice.application.service.FileNameFactory;
import com.example.storageservice.application.service.FileValidationService;
import com.example.storageservice.domain.model.FileStatus;
import com.example.storageservice.domain.model.FileVisibility;
import com.example.storageservice.domain.model.StoredObject;
import com.example.storageservice.domain.policy.StorageFilePermissionPolicy;
import com.example.storageservice.infrastructure.persistence.FileMetadataEntity;
import com.example.storageservice.infrastructure.persistence.FileMetadataRepository;
import com.example.storageservice.infrastructure.storage.StorageStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 11:50
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Service
@RequiredArgsConstructor
public class FileUseCases {
    private final FileMetadataRepository repo;
    private final FileNameFactory names;
    private final StorageStrategyFactory strategies;
    private final StorageFilePermissionPolicy policy;
    private final FileValidationService validator;
    private final ChecksumService checksum;
    private final FileMetadataMapper mapper;

    @Transactional
    public FileMetaDataResponse upload(MultipartFile file, FileVisibility visibility, String description, String tags, CurrentUser user) {
        if (!policy.canUpload(visibility, user)) throw new ForbiddenException("No upload permission");
        try {
            validator.validate(file);
            byte[] bytes = file.getBytes();
            var stored = strategies.current().store(names.sanitize(file.getOriginalFilename()), file.getContentType(), new ByteArrayInputStream(bytes), file.getSize());
            FileMetadataEntity e = new FileMetadataEntity();
            setupFileInfo(file, bytes, stored, e);
            e.setStorageProvider(stored.provider());
            e.setBucketName(stored.bucketName());
            e.setObjectKey(stored.objectKey());
            e.setVisibility(visibility);
            e.setOwnerId(user.id());
            e.setOwnerUsername(user.username());
            e.setDescription(description);
            e.setTags(tags);
            e.setCreatedBy(user.username());
            e.setStatus(FileStatus.ACTIVE);
            return mapper.toResponse(repo.save(e));
        } catch (IOException ex) {
            throw new BadRequestException("Cannot upload file");
        }
    }

    private void setupFileInfo(MultipartFile file, byte[] bytes, StoredObject stored, FileMetadataEntity e) {
        e.setOriginalName(names.sanitize(file.getOriginalFilename()));
        e.setStoredName(stored.storedName());
        e.setExtension(names.extensionOf(file.getOriginalFilename()));
        e.setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
        e.setFileSize(file.getSize());
        e.setChecksumSha256(checksum.sha256(bytes));
    }
}
