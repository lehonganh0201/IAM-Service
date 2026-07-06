package com.example.storageservice.application.dto.request;

import jakarta.validation.constraints.Size;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 14:16
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public record FileUpdateRequest(@Size(max = 1000) String description, @Size(max = 1000) String tags) {
}

