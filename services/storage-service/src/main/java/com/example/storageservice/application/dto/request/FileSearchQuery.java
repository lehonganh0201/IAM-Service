package com.example.storageservice.application.dto.request;

import com.example.storageservice.domain.model.FileVisibility;

import java.time.Instant;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 13:57
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public record FileSearchQuery(FileVisibility visibility, String keyword, String contentType, String extension,
                              String ownerId, Instant fromDate, Instant toDate) {
}
