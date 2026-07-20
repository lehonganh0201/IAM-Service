package com.example.userservice.application.dto.response;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 16:47
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public record ImportResultResponse(int totalRows, int validRows, int errorRows, List<ImportErrorItem> errors) {
}

