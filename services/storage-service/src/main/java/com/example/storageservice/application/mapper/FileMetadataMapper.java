package com.example.storageservice.application.mapper;

import com.example.storageservice.application.dto.response.FileMetaDataResponse;
import com.example.storageservice.infrastructure.persistence.FileMetadataEntity;
import org.mapstruct.Mapper;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 11:58
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Mapper(componentModel = "spring")
public interface FileMetadataMapper {
    FileMetaDataResponse toResponse(FileMetadataEntity e);
}
