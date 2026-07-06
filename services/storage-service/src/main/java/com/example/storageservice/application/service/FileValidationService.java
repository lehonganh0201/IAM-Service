package com.example.storageservice.application.service;

import com.example.commonlib.exception.BadRequestException;
import com.example.storageservice.config.StorageProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 11:58
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
public class FileValidationService {
    private final StorageProperties p;
    private final FileNameFactory f;

    public FileValidationService(StorageProperties p, FileNameFactory f) {
        this.p = p;
        this.f = f;
    }

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new BadRequestException("File must not be empty");
        if (file.getSize() > p.maxFileSizeBytes())
            throw new BadRequestException("File size exceeds limit");
        String e = f.extensionOf(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        if (!p.allowedExtensions().contains(e))
            throw new BadRequestException("File extension is not allowed: " + e);
        if (StringUtils.hasText(file.getContentType()) && !p.allowedMimeTypes().contains(file.getContentType()))
            throw new BadRequestException("MIME type is not allowed: " + file.getContentType());
        if (StringUtils.hasText(file.getOriginalFilename()) && (file.getOriginalFilename().contains("../") || file.getOriginalFilename().contains("..\\")))
            throw new BadRequestException("Invalid filename");
    }
}
