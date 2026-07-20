package com.example.userservice.infrastructure.client;

import com.example.userservice.application.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 15:52
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public interface StorageClientAdapter {
    FileUploadResponse uploadAvatar(MultipartFile file);

    void deleteFile(String fileId);
}

