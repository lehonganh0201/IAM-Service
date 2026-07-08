package com.example.iamservice.domain.dto.response;

import com.example.iamservice.domain.dto.importer.ImportErrorItem;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    08/07/2026 at 11:25
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public record ImportResultResponse(int totalRows, int validRows, int errorRows, List<ImportErrorItem> errors) {
}

