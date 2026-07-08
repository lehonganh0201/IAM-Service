package com.example.iamservice.domain.dto.importer;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    08/07/2026 at 11:32
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public record ImportErrorItem(int rowIndex, String field, Object value, String message) {
}

