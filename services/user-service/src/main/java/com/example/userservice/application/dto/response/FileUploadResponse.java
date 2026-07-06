package com.example.userservice.application.dto.response;

import java.util.UUID;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 15:53
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public record FileUploadResponse(UUID id, String originalName, String contentType, Long fileSize) {
}

