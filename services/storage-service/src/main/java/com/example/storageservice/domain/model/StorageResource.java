package com.example.storageservice.domain.model;

import org.springframework.core.io.Resource;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 11:32
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public record StorageResource(Resource resource, String contentType, long contentLength, String filename) {
}

