package com.example.iamservice.client;

import com.example.iamservice.domain.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    08/07/2026 at 13:18
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */


public interface StorageClientAdapter {
    FileUploadResponse uploadAvatar(MultipartFile file);

    void deleteFile(String fileId);
}
