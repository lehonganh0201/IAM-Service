package com.example.storageservice.application.usecase;

import com.example.commonlib.api.common.PageResponse;
import com.example.commonlib.exception.BadRequestException;
import com.example.commonlib.exception.ForbiddenException;
import com.example.commonlib.exception.NotFoundException;
import com.example.commonlib.security.CurrentUser;
import com.example.storageservice.application.dto.request.FileSearchQuery;
import com.example.storageservice.application.dto.request.FileUpdateRequest;
import com.example.storageservice.application.dto.response.FileMetaDataResponse;
import com.example.storageservice.application.mapper.FileMetadataMapper;
import com.example.storageservice.application.service.ChecksumService;
import com.example.storageservice.application.service.FileNameFactory;
import com.example.storageservice.application.service.FileValidationService;
import com.example.storageservice.config.StorageProperties;
import com.example.storageservice.domain.model.FileStatus;
import com.example.storageservice.domain.model.FileVisibility;
import com.example.storageservice.domain.model.StorageResource;
import com.example.storageservice.domain.model.StoredObject;
import com.example.storageservice.domain.policy.StorageFilePermissionPolicy;
import com.example.storageservice.infrastructure.persistence.FileMetadataEntity;
import com.example.storageservice.infrastructure.persistence.FileMetadataRepository;
import com.example.storageservice.infrastructure.persistence.FileMetadataSpecifications;
import com.example.storageservice.infrastructure.storage.StorageStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

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
    private final StorageProperties props;

    @Transactional
    public FileMetaDataResponse upload(MultipartFile file, FileVisibility visibility, String description, String tags, CurrentUser user) {
        if (!policy.canUpload(visibility, user)) throw new ForbiddenException("No upload permission");
        try {
            validator.validate(file);
            byte[] bytes = file.getBytes();
            StoredObject stored = strategies.current().store(names.sanitize(file.getOriginalFilename()), file.getContentType(), new ByteArrayInputStream(bytes), file.getSize());
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

    public FileMetaDataResponse get(UUID id, CurrentUser u) {
        FileMetadataEntity f = find(id);
        if (!policy.canRead(f, u)) throw new ForbiddenException("No read permission");
        return mapper.toResponse(f);
    }

    public PageResponse<FileMetaDataResponse> search(FileSearchQuery q, Pageable p) {
        return PageResponse.from(repo.findAll(
                FileMetadataSpecifications.byQuery(q),
                PageRequest.of(p.getPageNumber(), p.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt"))).map(mapper::toResponse));
    }


    public StorageResource download(UUID id, CurrentUser u) {
        FileMetadataEntity f = find(id);
        if (!policy.canDownload(f, u)) throw new ForbiddenException("No download permission");
        try {
            return strategies.byProvider(f.getStorageProvider()).load(f.getObjectKey(), f.getOriginalName(), f.getContentType());
        } catch (IOException e) {
            throw new BadRequestException("Cannot download file");
        }
    }

    @Transactional
    public FileMetaDataResponse update(UUID id, FileUpdateRequest req, CurrentUser u) {
        FileMetadataEntity f = find(id);
        if (!policy.canUpdate(f, u)) throw new ForbiddenException("No update permission");
        f.setDescription(req.description());
        f.setTags(req.tags());
        f.setUpdatedAt(Instant.now());
        f.setUpdatedBy(u.username());
        return mapper.toResponse(repo.save(f));
    }

    @Transactional
    public FileMetaDataResponse replaceContent(UUID id, MultipartFile file, CurrentUser u) {
        FileMetadataEntity f = find(id);
        if (!policy.canUpdate(f, u)) throw new ForbiddenException("No update permission");
        try {
            validator.validate(file);
            byte[] bytes = file.getBytes();
            StoredObject stored = strategies.byProvider(f.getStorageProvider()).store(names.sanitize(file.getOriginalFilename()), file.getContentType(), new ByteArrayInputStream(bytes), file.getSize());
            try {
                strategies.byProvider(f.getStorageProvider()).delete(f.getObjectKey());
            } catch (IOException ignored) {
            }
            setupFileInfo(file, bytes, stored, f);
            f.setBucketName(stored.bucketName());
            f.setObjectKey(stored.objectKey());
            f.setUpdatedAt(Instant.now());
            f.setUpdatedBy(u.username());
            return mapper.toResponse(repo.save(f));
        } catch (IOException ex) {
            throw new BadRequestException("Cannot replace file content");
        }
    }

    @Transactional
    public void delete(UUID id, CurrentUser u) {
        var f = find(id);
        if (!policy.canDelete(f, u)) throw new ForbiddenException("No delete permission");
        f.setStatus(FileStatus.DELETED);
        f.setDeletedAt(Instant.now());
        f.setDeletedBy(u.username());
        repo.save(f);
        if (props.hardDelete())
            try {
                strategies.byProvider(f.getStorageProvider()).delete(f.getObjectKey());
            } catch (IOException ignored) {
                throw new BadRequestException("Cannot delete file from storage");
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

    private FileMetadataEntity find(UUID id) {
        return repo.findByIdAndStatus(id, FileStatus.ACTIVE).orElseThrow(() -> new NotFoundException("File not found"));
    }
}
