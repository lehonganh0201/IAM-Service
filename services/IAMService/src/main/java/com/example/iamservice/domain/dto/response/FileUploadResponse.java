package com.example.iamservice.domain.dto.response;

import java.util.UUID;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    08/07/2026 at 13:19
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public record FileUploadResponse(UUID id, String originalName, String contentType, Long fileSize) {
}
